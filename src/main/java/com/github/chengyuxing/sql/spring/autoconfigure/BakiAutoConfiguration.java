package com.github.chengyuxing.sql.spring.autoconfigure;

import com.github.chengyuxing.common.script.IPipe;
import com.github.chengyuxing.common.utils.ReflectUtil;
import com.github.chengyuxing.sql.Baki;
import com.github.chengyuxing.sql.XQLFileManager;
import com.github.chengyuxing.sql.page.PageHelperProvider;
import com.github.chengyuxing.sql.support.SqlInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
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
    private final PlatformTransactionManager transactionManager;
    private final ApplicationArguments applicationArguments;

    public BakiAutoConfiguration(DataSource dataSource, BakiProperties bakiProperties, @Autowired(required = false) PlatformTransactionManager transactionManager, ApplicationArguments applicationArguments) {
        this.dataSource = dataSource;
        this.bakiProperties = bakiProperties;
        this.transactionManager = transactionManager;
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
            if (bakiProperties.getNamedParamPrefix() != ' ') {
                xqlFileManager.setNamedParamPrefix(bakiProperties.getNamedParamPrefix());
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
        baki.setXqlFileManager(xqlFileManager());
        baki.setBatchSize(bakiProperties.getBatchSize());
        if (bakiProperties.getNamedParamPrefix() != ' ') {
            baki.setNamedParamPrefix(baki.getNamedParamPrefix());
        }
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
        log.info("Baki initialized (Transaction managed by Spring)");
        return baki;
    }

    @Bean
    @ConditionalOnMissingBean
    public Tx tx() {
        return new Tx(transactionManager);
    }
}
