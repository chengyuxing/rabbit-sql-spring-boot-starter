package com.github.chengyuxing.sql.spring.autoconfigure;

import com.github.chengyuxing.common.AroundExecutor;
import com.github.chengyuxing.common.io.ClassPathResource;
import com.github.chengyuxing.common.script.pipe.IPipe;
import com.github.chengyuxing.common.util.ValueUtils;
import com.github.chengyuxing.sql.Baki;
import com.github.chengyuxing.sql.EntityManager;
import com.github.chengyuxing.sql.XQLFileManager;
import com.github.chengyuxing.sql.plugins.*;
import com.github.chengyuxing.sql.spring.SpringManagedBaki;
import com.github.chengyuxing.sql.spring.properties.*;

import com.github.chengyuxing.sql.types.Execution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@ConditionalOnClass(Baki.class)
@EnableConfigurationProperties({BakiProperties.class, XQLFileManagerProperties.class})
public class BakiAutoConfiguration {
    private static final Logger log = LoggerFactory.getLogger(BakiAutoConfiguration.class);
    public static final String XQL_CONFIG_LOCATION_NAME = "xql.config.location";
    public static final String XQL_CONFIG_CONSTANT_NAME = "xql.config.constants.";
    private final ApplicationArguments applicationArguments;
    private final BakiProperties bakiProperties;

    public BakiAutoConfiguration(@Autowired(required = false) ApplicationArguments applicationArguments, BakiProperties bakiProperties) {
        this.applicationArguments = applicationArguments;
        this.bakiProperties = bakiProperties;
    }

    private String findConfigFile() {
        // Priority 1
        // support custom xql-file-manager.yml location by command-line
        // e.g.
        // local file system
        // file:/usr/local/xql.config.oracle.yml
        // classpath
        // some/xql.config.oracle.yml
        if (applicationArguments != null && applicationArguments.containsOption(XQL_CONFIG_LOCATION_NAME)) {
            List<String> values = applicationArguments.getOptionValues(XQL_CONFIG_LOCATION_NAME);
            if (values != null && !values.isEmpty()) {
                String config = values.get(0);
                log.info("Load {} by {}", config, XQL_CONFIG_LOCATION_NAME);
                return config;
            }
        }

        // Priority 2
        // baki.xql-file-manager.configLocation
        XQLFileManagerProperties properties = bakiProperties.getXqlFileManager();
        if (!ObjectUtils.isEmpty(properties) && !ObjectUtils.isEmpty(properties.getConfigLocation())) {
            String config = properties.getConfigLocation();
            log.info("Load {} by baki.xql-file-manager.configLocation", config);
            return config;
        }

        // Priority 3
        // classpath xql-file-manager.yml
        if (new ClassPathResource(XQLFileManager.YML).exists()) {
            log.info("Load classpath default {}", XQLFileManager.YML);
            return XQLFileManager.YML;
        }

        return null;
    }

    private void updatePropertyByArguments(XQLFileManager xqlFileManager) {
        if (applicationArguments != null) {
            int constPrefixLen = XQL_CONFIG_CONSTANT_NAME.length();
            applicationArguments.getOptionNames().forEach(name -> {
                if (name.startsWith(XQL_CONFIG_CONSTANT_NAME) && name.length() > constPrefixLen) {
                    List<String> values = applicationArguments.getOptionValues(name);
                    if (values != null && !values.isEmpty()) {
                        String constName = name.substring(constPrefixLen);
                        String value = values.get(0);
                        log.debug("Update XQLFileManager constant {}={}", constName, value);
                        xqlFileManager.getConstants().put(constName, value);
                    }
                }
            });
        }
    }

    @Bean("rabbitXqlFileManager")
    @ConditionalOnMissingBean
    public XQLFileManager xqlFileManager() {
        String config = findConfigFile();

        boolean hasConfig = config != null;

        XQLFileManager xqlFileManager = hasConfig
                ? new XQLFileManager(config)
                : new XQLFileManager();

        XQLFileManagerProperties properties = bakiProperties.getXqlFileManager();

        if (!hasConfig && !ObjectUtils.isEmpty(properties)) {
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
            if (properties.getNamedParamPrefix() != ' ') {
                xqlFileManager.setNamedParamPrefix(properties.getNamedParamPrefix());
            }
        }

        updatePropertyByArguments(xqlFileManager);

        xqlFileManager.init();
        return xqlFileManager;
    }

    @Bean("rabbitEntityMetaProvider")
    @ConditionalOnMissingBean
    public EntityManager.EntityMetaProvider entityMetaProvider() {
        return new EntityManager.EntityMetaProvider() {
            @Override
            public String tableName(Class<?> clazz) {
                return clazz.getSimpleName();
            }

            @Override
            public EntityManager.ColumnMeta columnMeta(Field field) {
                return new EntityManager.ColumnMeta(field.getName());
            }

            @Override
            public Object columnValue(Field field, Object value) {
                return ValueUtils.adaptValue(field.getType(), value);
            }
        };
    }

    @Bean("rabbitBaki")
    @ConditionalOnMissingBean
    public Baki baki(@Autowired DataSource dataSource,
                     @Autowired(required = false) QueryCacheManager queryCacheManager,
                     @Autowired(required = false) XQLFileManager xqlFileManager,
                     @Autowired(required = false) SqlInterceptor sqlInterceptor,
                     @Autowired(required = false) PageHelperProvider pageHelperProvider,
                     @Autowired(required = false) StatementValueHandler statementValueHandler,
                     @Autowired(required = false) AroundExecutor<Execution> executionWatcher,
                     @Autowired(required = false) QueryTimeoutHandler queryTimeoutHandler,
                     @Autowired(required = false) EntityManager.EntityMetaProvider entityMetaProvider,
                     @Autowired(required = false) SqlInvokeHandler sqlInvokeHandler,
                     @Autowired(required = false) DatabaseInfoProvider databaseInfoProvider) {
        SpringManagedBaki baki = new SpringManagedBaki(dataSource);
        if (xqlFileManager != null) {
            baki.setXqlFileManager(xqlFileManager);
            log.info("Baki XQLFileManager enabled: {}", xqlFileManager.getClass().getName());
        }
        if (queryCacheManager != null) {
            baki.setQueryCacheManager(queryCacheManager);
            log.info("Baki QueryCacheManager enabled: {}", queryCacheManager.getClass().getName());
        }
        if (sqlInterceptor != null) {
            baki.setSqlInterceptor(sqlInterceptor);
            log.info("Baki SqlInterceptor enabled: {}", sqlInterceptor.getClass().getName());
        }
        if (pageHelperProvider != null) {
            baki.setGlobalPageHelperProvider(pageHelperProvider);
            log.info("Baki GlobalPageHelperProvider enabled: {}", pageHelperProvider.getClass().getName());
        }
        if (statementValueHandler != null) {
            baki.setStatementValueHandler(statementValueHandler);
            log.info("Baki StatementValueHandler enabled: {}", statementValueHandler.getClass().getName());
        }
        if (executionWatcher != null) {
            baki.setExecutionWatcher(executionWatcher);
            log.info("Baki ExecutionWatcher enabled: {}", executionWatcher.getClass().getName());
        }
        if (queryTimeoutHandler != null) {
            baki.setQueryTimeoutHandler(queryTimeoutHandler);
            log.info("Baki QueryTimeoutHandler enabled: {}", queryTimeoutHandler.getClass().getName());
        }
        if (entityMetaProvider != null) {
            baki.setEntityMetaProvider(entityMetaProvider);
            log.info("Baki EntityMetaProvider enabled: {}", entityMetaProvider.getClass().getName());
        }
        if (sqlInvokeHandler != null) {
            baki.setSqlInvokeHandler(sqlInvokeHandler);
            log.info("Baki SqlInvokeHandler enabled: {}", sqlInvokeHandler.getClass().getName());
        }
        if (databaseInfoProvider != null) {
            baki.setDatabaseInfoProvider(databaseInfoProvider);
            log.info("Baki DatabaseInfoProvider enabled: {}", databaseInfoProvider.getClass().getName());
        }
        if (!ObjectUtils.isEmpty(bakiProperties)) {
            baki.setBatchSize(bakiProperties.getBatchSize());
            baki.setPageKey(bakiProperties.getPageKey());
            baki.setSizeKey(bakiProperties.getSizeKey());
        }
        log.info("Baki({}) initialized (Transaction managed by Spring)", SpringManagedBaki.class.getName());
        return baki;
    }
}
