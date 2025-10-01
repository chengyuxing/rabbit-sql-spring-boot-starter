package com.github.chengyuxing.sql.spring.autoconfigure;

import com.github.chengyuxing.sql.Baki;
import com.github.chengyuxing.sql.XQLFileManager;
import com.github.chengyuxing.sql.spring.properties.*;
import com.github.chengyuxing.sql.spring.utils.BeanUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.util.ObjectUtils;

import javax.sql.DataSource;
import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

@Configuration
@ConditionalOnClass(Baki.class)
@EnableConfigurationProperties({SpringSecondaryDatasourceProperties.class, PrimaryBakiProperties.class, XQLFileManagerProperties.class})
@AutoConfigureAfter(DataSourceAutoConfiguration.class)
public class BakiAutoConfiguration implements InitializingBean {
    private static final Logger log = LoggerFactory.getLogger(BakiAutoConfiguration.class);
    public static final String XQL_CONFIG_LOCATION_NAME = "xql.config.location";
    private final DataSource dataSource;
    private final PrimaryBakiProperties bakiProperties;
    private final SpringSecondaryDatasourceProperties secondaryDatasourceProperties;
    private final ApplicationArguments applicationArguments;
    private final ConfigurableApplicationContext applicationContext;

    public BakiAutoConfiguration(DataSource dataSource, PrimaryBakiProperties bakiProperties, SpringSecondaryDatasourceProperties secondaryDatasourceProperties, ApplicationArguments applicationArguments, ConfigurableApplicationContext applicationContext) {
        this.dataSource = dataSource;
        this.bakiProperties = bakiProperties;
        this.secondaryDatasourceProperties = secondaryDatasourceProperties;
        this.applicationArguments = applicationArguments;
        this.applicationContext = applicationContext;
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean
    public XQLFileManager xqlFileManager() {
        // Priority 1
        // support custom xql-file-manager.yml location by command-line
        // e.g.
        // local file system
        // file:/usr/local/xql.config.oracle.yml
        // classpath
        // some/xql.config.oracle.yml
        String configLocation = null;
        if (applicationArguments.containsOption(XQL_CONFIG_LOCATION_NAME)) {
            configLocation = applicationArguments.getOptionValues(XQL_CONFIG_LOCATION_NAME).get(0);
            log.info("Load {} by {}", configLocation, XQL_CONFIG_LOCATION_NAME);
        }
        try {
            return BeanUtil.createXQLFileManager(configLocation, bakiProperties, applicationContext);
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException |
                 NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean
    public Baki baki() throws RuntimeException {
        DataSource myDataSource = dataSource;
        String datasourceName = bakiProperties.getDatasource();
        if (!ObjectUtils.isEmpty(datasourceName)) {
            myDataSource = applicationContext.getBean(datasourceName, DataSource.class);
        }
        try {
            return BeanUtil.createBaki(bakiProperties, myDataSource, xqlFileManager(), applicationContext);
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException |
                 NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // init secondary datasource
        Map<String, BakiDatasourceProperties> secondariesDs = secondaryDatasourceProperties.getSecondaries();
        if (!ObjectUtils.isEmpty(secondariesDs)) {
            for (Map.Entry<String, BakiDatasourceProperties> entry : secondariesDs.entrySet()) {
                String name = entry.getKey();
                BakiDatasourceProperties p = entry.getValue();
                try {
                    DataSource secondaryDatasource = BeanUtil.createDataSource(p.getClassName(), p.getProperties());
                    applicationContext.getBeanFactory().registerSingleton(name, secondaryDatasource);
                } catch (InvocationTargetException | NoSuchMethodException | InstantiationException |
                         IllegalAccessException | IntrospectionException | ClassNotFoundException e) {
                    throw new RuntimeException("Create secondary datasource error.", e);
                }
            }
        }
        // init secondary baki
        Map<String, BakiProperties> secondariesBaki = bakiProperties.getSecondaries();
        if (!ObjectUtils.isEmpty(secondariesBaki)) {
            for (Map.Entry<String, BakiProperties> entry : secondariesBaki.entrySet()) {
                String key = entry.getKey();
                BakiProperties p = entry.getValue();
                String datasourceName = p.getDatasource();
                DataSource dataSource = applicationContext.getBean(datasourceName, DataSource.class);
                XQLFileManager secondaryXqlFileManager = BeanUtil.createXQLFileManager(null, p, applicationContext);
                Baki secondaryBaki = BeanUtil.createBaki(p, dataSource, secondaryXqlFileManager, applicationContext);
                applicationContext.getBeanFactory().registerSingleton(key, secondaryBaki);
            }
        }
    }
}
