package com.github.chengyuxing.sql.spring.autoconfigure;

import com.github.chengyuxing.common.DataRow;
import com.github.chengyuxing.common.TiFunction;
import com.github.chengyuxing.common.io.ClassPathResource;
import com.github.chengyuxing.common.script.pipe.IPipe;
import com.github.chengyuxing.sql.Baki;
import com.github.chengyuxing.sql.XQLFileManager;
import com.github.chengyuxing.sql.annotation.SqlStatementType;
import com.github.chengyuxing.sql.plugins.*;
import com.github.chengyuxing.sql.spring.SpringManagedBaki;
import com.github.chengyuxing.sql.spring.properties.*;

import com.github.chengyuxing.sql.types.Execution;
import com.github.chengyuxing.sql.utils.JdbcUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

@Configuration
@ConditionalOnClass(Baki.class)
@EnableConfigurationProperties({BakiProperties.class, XQLFileManagerProperties.class})
@AutoConfigureAfter(name = {
        "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration",
        "org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration"
})
public class BakiAutoConfiguration {
    private static final Logger log = LoggerFactory.getLogger(BakiAutoConfiguration.class);
    public static final String XQL_CONFIG_LOCATION_NAME = "xql.config.location";
    private final DataSource dataSource;
    private final ApplicationArguments applicationArguments;
    private final BakiProperties bakiProperties;

    public BakiAutoConfiguration(DataSource dataSource, @Autowired(required = false) ApplicationArguments applicationArguments, BakiProperties bakiProperties) {
        this.dataSource = dataSource;
        this.applicationArguments = applicationArguments;
        this.bakiProperties = bakiProperties;
    }

    @Bean("rabbitXqlFileManager")
    @ConditionalOnMissingBean(XQLFileManager.class)
    public XQLFileManager xqlFileManager() {
        // Priority 1
        // support custom xql-file-manager.yml location by command-line
        // e.g.
        // local file system
        // file:/usr/local/xql.config.oracle.yml
        // classpath
        // some/xql.config.oracle.yml
        String configLocation = null;
        if (Objects.nonNull(applicationArguments) && applicationArguments.containsOption(XQL_CONFIG_LOCATION_NAME)) {
            List<String> values = applicationArguments.getOptionValues(XQL_CONFIG_LOCATION_NAME);
            if (values != null && !values.isEmpty()) {
                configLocation = values.get(0);
            }
            log.info("Load {} by {}", configLocation, XQL_CONFIG_LOCATION_NAME);
        }
        XQLFileManagerProperties properties = bakiProperties.getXqlFileManager();
        String myConfigLocation = configLocation;
        if (!ObjectUtils.isEmpty(properties) && !ObjectUtils.isEmpty(properties.getConfigLocation())) {
            myConfigLocation = properties.getConfigLocation();
            log.info("Load {} by baki.xql-file-manager.configLocation", myConfigLocation);
            // Priority 3
            // classpath xql-file-manager.yml
        } else if (new ClassPathResource(XQLFileManager.YML).exists()) {
            myConfigLocation = XQLFileManager.YML;
            log.info("Load classpath default {}", myConfigLocation);
        }

        XQLFileManager xqlFileManager = Objects.nonNull(myConfigLocation) ?
                new XQLFileManager(myConfigLocation) : new XQLFileManager();

        if (!ObjectUtils.isEmpty(properties)) {
            if (Objects.nonNull(myConfigLocation)) {
                log.info("Copy baki.xql-file-manager properties to {}", myConfigLocation);
            }
            if (!ObjectUtils.isEmpty(properties.getFiles())) {
                xqlFileManager.setFiles(properties.getFiles());
            }
            if (!ObjectUtils.isEmpty(properties.getConstants())) {
                xqlFileManager.setConstants(properties.getConstants());
            }
            if (!ObjectUtils.isEmpty(properties.getPipes())) {
                Map<String, String> pipes = new HashMap<>();
                for (@SuppressWarnings("rawtypes") Map.Entry<String, Class<? extends IPipe>> e : properties.getPipes().entrySet()) {
                    pipes.put(e.getKey(), e.getValue().getName());
                }
                xqlFileManager.setPipes(pipes);
            }
            if (StringUtils.hasText(properties.getCharset())) {
                xqlFileManager.setCharset(Charset.forName(properties.getCharset()));
            }
            if (StringUtils.hasText(properties.getDatabaseId())) {
                xqlFileManager.setDatabaseId(properties.getDatabaseId());
            }
            if (bakiProperties.getNamedParamPrefix() != ' ') {
                xqlFileManager.setNamedParamPrefix(bakiProperties.getNamedParamPrefix());
            }
        }
        xqlFileManager.init();
        return xqlFileManager;

    }

    @Bean("rabbitQueryCacheManager")
    @ConditionalOnMissingBean(QueryCacheManager.class)
    public QueryCacheManager queryCacheManager() {
        return new QueryCacheManager() {
            @Override
            public @NotNull Stream<DataRow> get(@NotNull String sql, Map<String, ?> args, @NotNull RawQueryProvider provider) {
                return Stream.empty();
            }

            @Override
            public boolean isAvailable(@NotNull String sql, Map<String, ?> args) {
                return false;
            }
        };
    }

    @Bean("rabbitSqlInterceptor")
    @ConditionalOnMissingBean(SqlInterceptor.class)
    public SqlInterceptor sqlInterceptor() {
        return (rawSql, parsedSql, args, metaData) -> parsedSql;
    }

    @Bean("rabbitPageHelperProvider")
    @ConditionalOnMissingBean(PageHelperProvider.class)
    public PageHelperProvider pageHelperProvider() {
        return (databaseMetaData, dbName, namedParamPrefix) -> null;
    }

    @Bean("rabbitStatementValueHandler")
    @ConditionalOnMissingBean(StatementValueHandler.class)
    public StatementValueHandler statementValueHandler() {
        return (ps, index, value, metaData) -> JdbcUtil.setStatementValue(ps, index, value);
    }

    @Bean("rabbitExecutionWatcher")
    @ConditionalOnMissingBean(ExecutionWatcher.class)
    public ExecutionWatcher executionWatcher() {
        return new ExecutionWatcher() {
            @Override
            public void onStart(Execution execution) {
            }

            @Override
            public void onStop(Execution execution, @Nullable Object result, @Nullable Throwable throwable) {
            }
        };
    }

    @Bean("rabbitQueryTimeoutHandler")
    @ConditionalOnMissingBean(QueryTimeoutHandler.class)
    public QueryTimeoutHandler queryTimeoutHandler() {
        return (sql, args) -> 0;
    }

    @Bean("rabbitEntityFieldMapper")
    @ConditionalOnMissingBean(EntityFieldMapper.class)
    public EntityFieldMapper entityFieldMapper() {
        return Field::getName;
    }

    @Bean("rabbitEntityValueMapper")
    @ConditionalOnMissingBean(EntityValueMapper.class)
    public EntityValueMapper entityValueMapper() {
        return (valueType, entityFieldType, value) -> null;
    }

    @Bean("rabbitSqlInvokeHandler")
    @ConditionalOnMissingBean(SqlInvokeHandler.class)
    public SqlInvokeHandler sqlInvokeHandler() {
        return type -> null;
    }

    @Bean("rabbitBaki")
    @ConditionalOnMissingBean(Baki.class)
    public Baki baki(QueryCacheManager queryCacheManager,
                     XQLFileManager xqlFileManager,
                     SqlInterceptor sqlInterceptor,
                     PageHelperProvider pageHelperProvider,
                     StatementValueHandler statementValueHandler,
                     ExecutionWatcher executionWatcher,
                     QueryTimeoutHandler queryTimeoutHandler,
                     EntityFieldMapper entityFieldMapper,
                     EntityValueMapper entityValueMapper,
                     SqlInvokeHandler sqlInvokeHandler) {
        SpringManagedBaki baki = new SpringManagedBaki(dataSource);
        baki.setXqlFileManager(xqlFileManager);
        baki.setQueryCacheManager(queryCacheManager);
        baki.setSqlInterceptor(sqlInterceptor);
        baki.setGlobalPageHelperProvider(pageHelperProvider);
        baki.setStatementValueHandler(statementValueHandler);
        baki.setExecutionWatcher(executionWatcher);
        baki.setQueryTimeoutHandler(queryTimeoutHandler);
        baki.setEntityFieldMapper(entityFieldMapper);
        baki.setEntityValueMapper(entityValueMapper);
        baki.setSqlInvokeHandler(sqlInvokeHandler);
        if (!ObjectUtils.isEmpty(bakiProperties)) {
            if (bakiProperties.getNamedParamPrefix() != ' ') {
                baki.setNamedParamPrefix(bakiProperties.getNamedParamPrefix());
            }
            baki.setBatchSize(bakiProperties.getBatchSize());
            baki.setPageKey(bakiProperties.getPageKey());
            baki.setSizeKey(bakiProperties.getSizeKey());
        }
        log.info("Baki({}) initialized (Transaction managed by Spring)", SpringManagedBaki.class.getName());
        return baki;
    }
}
