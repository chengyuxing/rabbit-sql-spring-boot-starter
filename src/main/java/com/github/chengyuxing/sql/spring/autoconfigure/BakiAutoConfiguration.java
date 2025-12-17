package com.github.chengyuxing.sql.spring.autoconfigure;

import com.github.chengyuxing.common.io.ClassPathResource;
import com.github.chengyuxing.common.script.pipe.IPipe;
import com.github.chengyuxing.sql.Baki;
import com.github.chengyuxing.sql.EntityManager;
import com.github.chengyuxing.sql.XQLFileManager;
import com.github.chengyuxing.sql.plugins.*;
import com.github.chengyuxing.sql.spring.SpringManagedBaki;
import com.github.chengyuxing.sql.spring.properties.*;

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
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

    @Bean("rabbitBaki")
    @ConditionalOnMissingBean(Baki.class)
    public Baki baki(@Autowired(required = false) QueryCacheManager queryCacheManager,
                     @Autowired(required = false) XQLFileManager xqlFileManager,
                     @Autowired(required = false) SqlInterceptor sqlInterceptor,
                     @Autowired(required = false) PageHelperProvider pageHelperProvider,
                     @Autowired(required = false) StatementValueHandler statementValueHandler,
                     @Autowired(required = false) ExecutionWatcher executionWatcher,
                     @Autowired(required = false) QueryTimeoutHandler queryTimeoutHandler,
                     @Autowired(required = false) EntityManager.EntityMetaProvider entityMetaProvider,
                     @Autowired(required = false) SqlInvokeHandler sqlInvokeHandler) {
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
