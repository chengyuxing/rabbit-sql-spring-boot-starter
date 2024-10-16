package com.github.chengyuxing.sql.spring.autoconfigure;

import com.github.chengyuxing.common.script.expression.IPipe;
import com.github.chengyuxing.common.utils.ReflectUtil;
import com.github.chengyuxing.sql.Baki;
import com.github.chengyuxing.sql.XQLFileManager;
import com.github.chengyuxing.sql.page.PageHelperProvider;
import com.github.chengyuxing.sql.support.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

@Configuration
@ConditionalOnClass(Baki.class)
@ConditionalOnSingleCandidate(DataSource.class)
@EnableConfigurationProperties({BakiProperties.class, XQLFileManagerProperties.class})
@AutoConfigureAfter(DataSourceAutoConfiguration.class)
public class BakiAutoConfiguration {
    private static final Logger log = LoggerFactory.getLogger(BakiAutoConfiguration.class);
    public static final String XQL_CONFIG_LOCATION_NAME = "xql.config.location";
    private final DataSource dataSource;
    private final BakiProperties bakiProperties;
    private final ApplicationArguments applicationArguments;

    public BakiAutoConfiguration(DataSource dataSource, BakiProperties bakiProperties, ApplicationArguments applicationArguments) {
        this.dataSource = dataSource;
        this.bakiProperties = bakiProperties;
        this.applicationArguments = applicationArguments;
    }

    @Bean
    @ConditionalOnMissingBean
    public XQLFileManager xqlFileManager() {
        XQLFileManager xqlFileManager;
        // support custom xql-file-manager.properties location by command-line
        // e.g:
        // local file system
        // file:/usr/local/xql.config.oracle.yml
        // classpath
        // some/xql.config.oracle.yml
        if (applicationArguments.containsOption(XQL_CONFIG_LOCATION_NAME)) {
            xqlFileManager = new XQLFileManager(applicationArguments.getOptionValues(XQL_CONFIG_LOCATION_NAME).get(0));
            xqlFileManager.init();
            return xqlFileManager;
        }

        XQLFileManagerProperties properties = bakiProperties.getXqlFileManager();
        // init read the default xql-file-manager.yml if exists.
        xqlFileManager = new XQLFileManager();
        // override default setting if application.yml 'baki.xql-file-manager' configured.
        if (!ObjectUtils.isEmpty(properties)) {
            // override default xql-file-manager.yml if custom location configured.
            if (!ObjectUtils.isEmpty(properties.getConfigLocation())) {
                xqlFileManager = new XQLFileManager(properties.getConfigLocation());
            }
            if (!ObjectUtils.isEmpty(properties.getFiles())) {
                xqlFileManager.setFiles(properties.getFiles());
            }
            if (!ObjectUtils.isEmpty(properties.getConstants())) {
                xqlFileManager.setConstants(properties.getConstants());
            }
            if (!ObjectUtils.isEmpty(properties.getPipes())) {
                Map<String, IPipe<?>> pipeInstances = new HashMap<>();
                try {
                    for (@SuppressWarnings("rawtypes") Map.Entry<String, Class<? extends IPipe>> e : properties.getPipes().entrySet()) {
                        pipeInstances.put(e.getKey(), ReflectUtil.getInstance(e.getValue()));
                    }
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                         NoSuchMethodException e) {
                    throw new IllegalStateException("create pipe instance error: ", e);
                }
                xqlFileManager.setPipeInstances(pipeInstances);
            }
            if (StringUtils.hasText(properties.getCharset())) {
                xqlFileManager.setCharset(Charset.forName(properties.getCharset()));
            }
            if (StringUtils.hasLength(properties.getDelimiter())) {
                xqlFileManager.setDelimiter(properties.getDelimiter());
            }
            if (StringUtils.hasText(properties.getDatabaseId())) {
                xqlFileManager.setDatabaseId(properties.getDatabaseId());
            }
            if (bakiProperties.getNamedParamPrefix() != ' ') {
                xqlFileManager.setNamedParamPrefix(bakiProperties.getNamedParamPrefix());
            }
            if (!ObjectUtils.isEmpty(bakiProperties.getTemplateFormatter())) {
                try {
                    TemplateFormatter templateFormatter = ReflectUtil.getInstance(bakiProperties.getTemplateFormatter());
                    xqlFileManager.setTemplateFormatter(templateFormatter);
                } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                         IllegalAccessException e) {
                    throw new RuntimeException("configure templateFormatter error.", e);
                }
            }
        }
        xqlFileManager.init();
        return xqlFileManager;
    }

    @Bean
    @ConditionalOnMissingBean
    public Baki baki() throws RuntimeException {
        SpringManagedBaki baki = new SpringManagedBaki(dataSource);
        if (ObjectUtils.isEmpty(bakiProperties)) {
            return baki;
        }
        baki.setBatchSize(bakiProperties.getBatchSize());
        if (bakiProperties.getNamedParamPrefix() != ' ') {
            baki.setNamedParamPrefix(bakiProperties.getNamedParamPrefix());
        }
        baki.setReloadXqlOnGet(bakiProperties.isReloadXqlOnGet());
        baki.setAutoXFMConfig(bakiProperties.isAutoXFMConfig());
        XQLFileManager xqlFileManager = xqlFileManager();
        if (!ObjectUtils.isEmpty(bakiProperties.getGlobalPageHelperProvider())) {
            try {
                PageHelperProvider pageHelperProvider = ReflectUtil.getInstance(bakiProperties.getGlobalPageHelperProvider());
                baki.setGlobalPageHelperProvider(pageHelperProvider);
            } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                     IllegalAccessException e) {
                throw new IllegalStateException("configure globalPageHelperProvider error: ", e);
            }
        }
        if (!ObjectUtils.isEmpty(bakiProperties.getSqlInterceptor())) {
            try {
                SqlInterceptor sqlInterceptor = ReflectUtil.getInstance(bakiProperties.getSqlInterceptor());
                baki.setSqlInterceptor(sqlInterceptor);
            } catch (InvocationTargetException | NoSuchMethodException | InstantiationException |
                     IllegalAccessException e) {
                throw new IllegalStateException("configure sqlInterceptor error: ", e);
            }
        }
        if (!ObjectUtils.isEmpty(bakiProperties.getStatementValueHandler())) {
            try {
                StatementValueHandler statementValueHandler = ReflectUtil.getInstance(bakiProperties.getStatementValueHandler());
                baki.setStatementValueHandler(statementValueHandler);
            } catch (InvocationTargetException | NoSuchMethodException | InstantiationException |
                     IllegalAccessException e) {
                throw new IllegalStateException("configure statementValueHandler error.", e);
            }
        }
        if (!ObjectUtils.isEmpty(bakiProperties.getAfterParseDynamicSql())) {
            try {
                AfterParseDynamicSql afterParseDynamicSql = ReflectUtil.getInstance(bakiProperties.getAfterParseDynamicSql());
                baki.setAfterParseDynamicSql(afterParseDynamicSql);
            } catch (InvocationTargetException | NoSuchMethodException | InstantiationException |
                     IllegalAccessException e) {
                throw new IllegalStateException("configure afterParseDynamicSql error.", e);
            }
        }
        if (!ObjectUtils.isEmpty(bakiProperties.getTemplateFormatter())) {
            try {
                TemplateFormatter templateFormatter = ReflectUtil.getInstance(bakiProperties.getTemplateFormatter());
                baki.setTemplateFormatter(templateFormatter);
            } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                     IllegalAccessException e) {
                throw new RuntimeException("configure templateFormatter error.", e);
            }
        }
        if (!ObjectUtils.isEmpty(bakiProperties.getNamedParamFormatter())) {
            try {
                NamedParamFormatter namedParamFormatter = ReflectUtil.getInstance(bakiProperties.getNamedParamFormatter());
                baki.setNamedParamFormatter(namedParamFormatter);
            } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                     IllegalAccessException e) {
                throw new RuntimeException("configure namedParamFormatter error.", e);
            }
        }
        if (!ObjectUtils.isEmpty(bakiProperties.getSqlWatcher())) {
            try {
                SqlWatcher sqlWatcher = ReflectUtil.getInstance(bakiProperties.getSqlWatcher());
                baki.setSqlWatcher(sqlWatcher);
            } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                     IllegalAccessException e) {
                throw new RuntimeException("configure sqlWatcher error.", e);
            }
        }
        if (!ObjectUtils.isEmpty(bakiProperties.getQueryTimeoutHandler())) {
            try {
                QueryTimeoutHandler queryTimeoutHandler = ReflectUtil.getInstance(bakiProperties.getQueryTimeoutHandler());
                baki.setQueryTimeoutHandler(queryTimeoutHandler);
            } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                     IllegalAccessException e) {
                throw new RuntimeException("configure queryTimeoutHandler error.", e);
            }
        }
        if (!ObjectUtils.isEmpty(bakiProperties.getQueryCacheManager())) {
            try {
                QueryCacheManager queryCacheManager = ReflectUtil.getInstance(bakiProperties.getQueryCacheManager());
                baki.setQueryCacheManager(queryCacheManager);
            } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                     IllegalAccessException e) {
                throw new RuntimeException("configure queryCacheManager error.", e);
            }
        }
        baki.setPageKey(bakiProperties.getPageKey());
        baki.setSizeKey(bakiProperties.getSizeKey());
        baki.setXqlFileManager(xqlFileManager);
        log.info("Baki initialized (Transaction managed by Spring)");
        return baki;
    }
}
