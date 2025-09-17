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
import org.springframework.context.ApplicationContext;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.beans.IntrospectionException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.*;

public final class BeanUtil {
    private final static Logger log = LoggerFactory.getLogger(BeanUtil.class);

    public static SpringManagedBaki createBaki(BakiProperties bakiProperties, DataSource dataSource, XQLFileManager xqlFileManager, ApplicationContext context) throws InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {
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
            PageHelperProvider pageHelperProvider = getInstanceIfContext(bakiProperties.getGlobalPageHelperProvider(), context);
            baki.setGlobalPageHelperProvider(pageHelperProvider);
        }
        if (!ObjectUtils.isEmpty(bakiProperties.getSqlInterceptor())) {
            SqlInterceptor sqlInterceptor = getInstanceIfContext(bakiProperties.getSqlInterceptor(), context);
            baki.setSqlInterceptor(sqlInterceptor);
        }
        if (!ObjectUtils.isEmpty(bakiProperties.getStatementValueHandler())) {
            StatementValueHandler statementValueHandler = getInstanceIfContext(bakiProperties.getStatementValueHandler(), context);
            baki.setStatementValueHandler(statementValueHandler);
        }
        if (!ObjectUtils.isEmpty(bakiProperties.getSqlParseChecker())) {
            SqlParseChecker sqlParseChecker = getInstanceIfContext(bakiProperties.getSqlParseChecker(), context);
            baki.setSqlParseChecker(sqlParseChecker);
        }
        if (!ObjectUtils.isEmpty(bakiProperties.getTemplateFormatter())) {
            TemplateFormatter templateFormatter = getInstanceIfContext(bakiProperties.getTemplateFormatter(), context);
            baki.setTemplateFormatter(templateFormatter);
        }
        if (!ObjectUtils.isEmpty(bakiProperties.getNamedParamFormatter())) {
            NamedParamFormatter namedParamFormatter = getInstanceIfContext(bakiProperties.getNamedParamFormatter(), context);
            baki.setNamedParamFormatter(namedParamFormatter);
        }
        if (!ObjectUtils.isEmpty(bakiProperties.getExecutionWatcher())) {
            ExecutionWatcher sqlWatcher = getInstanceIfContext(bakiProperties.getExecutionWatcher(), context);
            baki.setExecutionWatcher(sqlWatcher);
        }
        if (!ObjectUtils.isEmpty(bakiProperties.getQueryTimeoutHandler())) {
            QueryTimeoutHandler queryTimeoutHandler = getInstanceIfContext(bakiProperties.getQueryTimeoutHandler(), context);
            baki.setQueryTimeoutHandler(queryTimeoutHandler);
        }
        if (!ObjectUtils.isEmpty(bakiProperties.getQueryCacheManager())) {
            QueryCacheManager queryCacheManager = getInstanceIfContext(bakiProperties.getQueryCacheManager(), context);
            baki.setQueryCacheManager(queryCacheManager);
        }
        if (!ObjectUtils.isEmpty(bakiProperties.getEntityFieldMapper())) {
            EntityFieldMapper entityFieldMapper = getInstanceIfContext(bakiProperties.getEntityFieldMapper(), context);
            baki.setEntityFieldMapper(entityFieldMapper);
        }
        if (!ObjectUtils.isEmpty(bakiProperties.getEntityValueMapper())) {
            EntityValueMapper entityValueMapper = getInstanceIfContext(bakiProperties.getEntityValueMapper(), context);
            baki.setEntityValueMapper(entityValueMapper);
        }
        log.info("Baki({}) initialized (Transaction managed by Spring)", SpringManagedBaki.class.getName());
        return baki;
    }

    public static XQLFileManager createXQLFileManager(String configLocation, BakiProperties bakiProperties, ApplicationContext context) throws InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {
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
                TemplateFormatter templateFormatter = getInstanceIfContext(bakiProperties.getTemplateFormatter(), context);
                xqlFileManager.setTemplateFormatter(templateFormatter);
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

    public static <T> T getInstanceIfContext(Class<T> clazz, ApplicationContext context) throws InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {
        @SuppressWarnings("unchecked") Constructor<T>[] constructors = (Constructor<T>[]) clazz.getDeclaredConstructors();
        Optional<Constructor<T>> ctxConstructor = Arrays.stream(constructors)
                .filter(constructor -> constructor.getParameterCount() == 1 && ApplicationContext.class.isAssignableFrom(constructor.getParameterTypes()[0]))
                .findFirst();
        if (ctxConstructor.isPresent()) {
            Constructor<T> constructor = ctxConstructor.get();
            constructor.setAccessible(true);
            return constructor.newInstance(context);
        }
        Constructor<T> constructor = clazz.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance();
    }
}
