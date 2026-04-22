package com.github.chengyuxing.sql.spring.components.cache;

import com.github.chengyuxing.common.DataRow;
import com.github.chengyuxing.common.util.StringUtils;
import com.github.chengyuxing.sql.plugins.QueryCacheManager;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class RedisQueryCacheManager implements QueryCacheManager {
    private final ExecutorService refreshPool = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "Rabbit-SQL Query Cache Refresh Thread");
        thread.setDaemon(true);
        return thread;
    });

    final RedisTemplate<Object, Object> redisTemplate;

    public RedisQueryCacheManager(RedisTemplate<Object, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Create cache entry object.
     *
     * @param sql SQL or reference
     * @return cache entry object
     */
    protected abstract @NotNull CacheEntry createCacheEntry(String sql);

    /**
     * Set async concurrent refresh redis lock key expiration timeout.
     *
     * @return timeout in seconds
     */
    protected abstract int lockKeyExpirationTimeout();

    @Override
    public @NotNull Stream<DataRow> get(@NotNull String sql, Map<String, ?> args, @NotNull RawQueryProvider provider) {
        String key = uniqueKey(sql, args);
        long now = System.currentTimeMillis();
        CacheEntry entry = (CacheEntry) redisTemplate.opsForValue().get(key);
        if (entry == null || now >= entry.hardExpireAt) {
            List<DataRow> result = new ArrayList<>();
            return provider.query()
                    .peek(result::add)
                    .onClose(() -> saveEntry(sql, key, result));
        }
        if (now < entry.softExpireAt) {
            return entry.value.stream();
        }
        asyncRefresh(sql, args, provider);
        return entry.value.stream();
    }

    protected void asyncRefresh(@NotNull String sql, Map<String, ?> args, @NotNull RawQueryProvider provider) {
        String key = uniqueKey(sql, args);
        String lockKey = "lock:" + key;
        // Here, an expiration time for the lock is set to prevent it from being occupied for a long time,
        // which could prevent other threads from obtaining the data
        Boolean ok = redisTemplate.opsForValue().setIfAbsent(lockKey, 1, lockKeyExpirationTimeout(), TimeUnit.SECONDS);
        if (ok == null || !ok) {
            return;
        }
        refreshPool.execute(() -> {
            // Double check to prevent breakdown
            CacheEntry entry = (CacheEntry) redisTemplate.opsForValue().get(key);
            // If the cache has not yet expired, then cancel the query request
            if (entry != null && System.currentTimeMillis() < entry.softExpireAt) {
                return;
            }
            try (Stream<DataRow> s = provider.query()) {
                List<DataRow> result = s.collect(Collectors.toList());
                saveEntry(sql, key, result);
            } finally {
                // release the lock
                redisTemplate.delete(lockKey);
            }
        });
    }

    protected void saveEntry(@NotNull String sql, @NotNull String key, List<DataRow> value) {
        CacheEntry entry = createCacheEntry(sql);
        entry.value = value;
        redisTemplate.opsForValue().set(key, entry, entry.hardExpireAt, TimeUnit.MILLISECONDS);
    }

    protected @NotNull String uniqueKey(@NotNull String sql, Map<String, ?> args) {
        String argsStr = "";
        if (args != null && !args.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            args.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(e -> sb.append(e.getKey()).append("=").append(e.getValue()));
            argsStr = "@" + StringUtils.hash(sb.toString(), "MD5");
        }
        if (sql.startsWith("&")) {
            return sql + argsStr;
        }
        return StringUtils.hash(sql, "MD5") + argsStr;
    }
}
