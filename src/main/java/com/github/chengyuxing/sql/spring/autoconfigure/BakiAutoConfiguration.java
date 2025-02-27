package com.github.chengyuxing.sql.spring.autoconfigure;

import com.github.chengyuxing.sql.Baki;
import com.github.chengyuxing.sql.XQLFileManager;
import com.github.chengyuxing.sql.spring.properties.BakiDatasourceProperties;
import com.github.chengyuxing.sql.spring.properties.BakiProperties;
import com.github.chengyuxing.sql.spring.properties.PrimaryBakiProperties;
import com.github.chengyuxing.sql.spring.properties.XQLFileManagerProperties;
import com.github.chengyuxing.sql.spring.utils.BeanUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.util.ObjectUtils;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

@Configuration
@ConditionalOnClass(Baki.class)
@EnableConfigurationProperties({PrimaryBakiProperties.class, XQLFileManagerProperties.class, BakiDatasourceProperties.class})
@AutoConfiguration
public class BakiAutoConfiguration {
    private static final Logger log = LoggerFactory.getLogger(BakiAutoConfiguration.class);
    public static final String XQL_CONFIG_LOCATION_NAME = "xql.config.location";
    private final DataSource dataSource;
    private final PrimaryBakiProperties bakiProperties;
    private final ApplicationArguments applicationArguments;
    private final ConfigurableApplicationContext applicationContext;

    public BakiAutoConfiguration(@Autowired(required = false) DataSource dataSource, PrimaryBakiProperties bakiProperties, ApplicationArguments applicationArguments, ConfigurableApplicationContext applicationContext) {
        this.dataSource = dataSource;
        this.bakiProperties = bakiProperties;
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
        return BeanUtil.createXQLFileManager(configLocation, bakiProperties);
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean
    public Baki baki() throws RuntimeException {
        DataSource myDataSource = dataSource;
        BakiDatasourceProperties bdp = bakiProperties.getDatasource();
        if (!ObjectUtils.isEmpty(bdp)) {
            try {
                myDataSource = BeanUtil.createDataSource(bdp.getClassName(), bdp.getProperties());
            } catch (InvocationTargetException | NoSuchMethodException | InstantiationException |
                     IllegalAccessException | IntrospectionException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return BeanUtil.createBaki(bakiProperties, myDataSource, xqlFileManager());
    }

    @PostConstruct
    public void initialize() {
        Map<String, BakiProperties> secondary = bakiProperties.getSecondary();
        if (!ObjectUtils.isEmpty(secondary)) {
            for (Map.Entry<String, BakiProperties> entry : secondary.entrySet()) {
                String key = entry.getKey();
                BakiProperties sbp = entry.getValue();
                BakiDatasourceProperties bdp = sbp.getDatasource();
                try {
                    DataSource secondaryDs = BeanUtil.createDataSource(bdp.getClassName(), bdp.getProperties());
                    XQLFileManager secondaryXqlFileManager = BeanUtil.createXQLFileManager(null, sbp);
                    Baki secondaryBaki = BeanUtil.createBaki(sbp, secondaryDs, secondaryXqlFileManager);
                    applicationContext.getBeanFactory().registerSingleton(key, secondaryBaki);
                } catch (InvocationTargetException | NoSuchMethodException | InstantiationException |
                         IllegalAccessException | IntrospectionException | ClassNotFoundException e) {
                    throw new RuntimeException("Create secondary baki error.", e);
                }
            }
        }
    }
}
