package com.github.chengyuxing.sql.spring.utils;

import com.github.chengyuxing.common.io.ClassPathResource;
import com.github.chengyuxing.common.script.expression.IPipe;
import com.github.chengyuxing.common.utils.ReflectUtil;
import com.github.chengyuxing.common.utils.StringUtil;
import com.github.chengyuxing.sql.XQLFileManager;
import com.github.chengyuxing.sql.plugins.*;
import com.github.chengyuxing.sql.spring.SpringManagedBaki;
import com.github.chengyuxing.sql.spring.properties.BakiProperties;
import com.github.chengyuxing.sql.spring.properties.XQLFileManagerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class BeanUtil {
    private final static Logger log = LoggerFactory.getLogger(BeanUtil.class);

    public static SpringManagedBaki createBaki(BakiProperties bakiProperties, DataSource dataSource, XQLFileManager xqlFileManager) {
        SpringManagedBaki baki = new SpringManagedBaki(dataSource);
        if (ObjectUtils.isEmpty(bakiProperties)) {
            return baki;
        }
        if (bakiProperties.getNamedParamPrefix() != ' ') {
            baki.setNamedParamPrefix(bakiProperties.getNamedParamPrefix());
        }
        baki.setBatchSize(bakiProperties.getBatchSize());
        baki.setPageKey(bakiProperties.getPageKey());
        baki.setSizeKey(bakiProperties.getSizeKey());
        baki.setXqlFileManager(xqlFileManager);
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
        if (!ObjectUtils.isEmpty(bakiProperties.getSqlParseChecker())) {
            try {
                SqlParseChecker sqlParseChecker = ReflectUtil.getInstance(bakiProperties.getSqlParseChecker());
                baki.setSqlParseChecker(sqlParseChecker);
            } catch (InvocationTargetException | NoSuchMethodException | InstantiationException |
                     IllegalAccessException e) {
                throw new IllegalStateException("configure sqlParseChecker error.", e);
            }
        }
        if (!ObjectUtils.isEmpty(bakiProperties.getTemplateFormatter())) {
            try {
                TemplateFormatter templateFormatter = ReflectUtil.getInstance(bakiProperties.getTemplateFormatter());
                baki.setTemplateFormatter(templateFormatter);
            } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                     IllegalAccessException e) {
                throw new IllegalStateException("configure templateFormatter error.", e);
            }
        }
        if (!ObjectUtils.isEmpty(bakiProperties.getNamedParamFormatter())) {
            try {
                NamedParamFormatter namedParamFormatter = ReflectUtil.getInstance(bakiProperties.getNamedParamFormatter());
                baki.setNamedParamFormatter(namedParamFormatter);
            } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                     IllegalAccessException e) {
                throw new IllegalStateException("configure namedParamFormatter error.", e);
            }
        }
        if (!ObjectUtils.isEmpty(bakiProperties.getSqlWatcher())) {
            try {
                SqlWatcher sqlWatcher = ReflectUtil.getInstance(bakiProperties.getSqlWatcher());
                baki.setSqlWatcher(sqlWatcher);
            } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                     IllegalAccessException e) {
                throw new IllegalStateException("configure sqlWatcher error.", e);
            }
        }
        if (!ObjectUtils.isEmpty(bakiProperties.getQueryTimeoutHandler())) {
            try {
                QueryTimeoutHandler queryTimeoutHandler = ReflectUtil.getInstance(bakiProperties.getQueryTimeoutHandler());
                baki.setQueryTimeoutHandler(queryTimeoutHandler);
            } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                     IllegalAccessException e) {
                throw new IllegalStateException("configure queryTimeoutHandler error.", e);
            }
        }
        if (!ObjectUtils.isEmpty(bakiProperties.getQueryCacheManager())) {
            try {
                QueryCacheManager queryCacheManager = ReflectUtil.getInstance(bakiProperties.getQueryCacheManager());
                baki.setQueryCacheManager(queryCacheManager);
            } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                     IllegalAccessException e) {
                throw new IllegalStateException("configure queryCacheManager error.", e);
            }
        }
        log.info("Baki({}) initialized (Transaction managed by Spring)", SpringManagedBaki.class.getName());
        return baki;
    }

    public static XQLFileManager createXQLFileManager(String configLocation, BakiProperties bakiProperties) {
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
                    throw new IllegalStateException("configure templateFormatter error.", e);
                }
            }
        }
        xqlFileManager.init();
        return xqlFileManager;
    }

    public static DataSource createDataSource(Class<? extends DataSource> clazz, Map<String, Object> properties) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, IntrospectionException, ClassNotFoundException {
        Class<? extends DataSource> myClass = clazz;
        if (ObjectUtils.isEmpty(myClass)) {
            //noinspection unchecked
            myClass = (Class<? extends DataSource>) Class.forName("com.zaxxer.hikari.HikariDataSource");
        }
        DataSource dataSource = ReflectUtil.getInstance(myClass);
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            String key = StringUtil.camelize(entry.getKey());
            Object value = entry.getValue();
            Method setter = myClass.getMethod("set" + key.substring(0, 1).toUpperCase() + key.substring(1), value.getClass());
            setter.invoke(dataSource, value);
        }
        return dataSource;
    }
}
