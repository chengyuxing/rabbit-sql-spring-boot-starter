package com.github.chengyuxing.sql.spring.components;

import com.github.chengyuxing.sql.XQLFileManager;
import com.github.chengyuxing.sql.spring.components.cache.CacheEntry;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class CommonUtils {
    /**
     * Create cache entry by XQL metadata property {@code -- @cache softTimeout,hardTimeout}, e.g.
     * <blockquote><pre>
     *     -- @cache 1m,5m
     * </pre></blockquote>
     *
     * @param xqlFileManager XQLFileManager
     * @param sqlRef         SQL reference name
     * @return cache entry object
     */
    public static @NotNull CacheEntry createCacheEntryByXQLMetadata(@NotNull XQLFileManager xqlFileManager, @NotNull String sqlRef) {
        CacheEntry entry = new CacheEntry();

        String sqlName = sqlRef.substring(1);

        if (xqlFileManager.contains(sqlName)) {
            XQLFileManager.Sql sql = xqlFileManager.getSqlObject(sqlName);
            Map<String, String> metadata = sql.getMetadata();
            String[] p = metadata.get("cache").split(",");
            entry.setSoftTimeout(TimeUnit.SECONDS.toMillis(Integer.parseInt(p[0])));
            entry.setHardTimeout(TimeUnit.SECONDS.toMillis(Integer.parseInt(p[1])));
        }

        return entry;
    }
}
