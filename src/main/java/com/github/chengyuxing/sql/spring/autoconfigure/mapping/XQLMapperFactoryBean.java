package com.github.chengyuxing.sql.spring.autoconfigure.mapping;

import com.github.chengyuxing.common.util.StringUtils;
import com.github.chengyuxing.sql.BakiDao;
import com.github.chengyuxing.sql.XQLInvocationHandler;
import com.github.chengyuxing.sql.util.XQLMapperUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Map;

public class XQLMapperFactoryBean<T> implements FactoryBean<T>, ApplicationContextAware {
    private static final Logger log = LoggerFactory.getLogger(XQLMapperFactoryBean.class);

    private final Class<T> mapperInterface;
    private ApplicationContext applicationContext;

    public XQLMapperFactoryBean(Class<T> mapperClass) {
        this.mapperInterface = mapperClass;
    }

    @Override
    public T getObject() throws Exception {
        return XQLMapperUtils.getProxyInstance(mapperInterface, new XQLInvocationHandler() {
            @Override
            protected @NotNull BakiDao baki() {
                return getTargetBaki();
            }
        });
    }

    @Override
    public Class<?> getObjectType() {
        return mapperInterface;
    }

    @Override
    public void setApplicationContext(@NotNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    private BakiDao getTargetBaki() {
        Map<String, BakiDao> map = applicationContext.getBeansOfType(BakiDao.class);
        if (map.size() == 1) {
            log.debug("Unique Baki detected and injected.");
            return applicationContext.getBean(BakiDao.class);
        }
        String defaultName = getBakiNameRelatedMapper();
        log.debug("Multiple Baki detected and inject by name '{}'.", defaultName);
        return map.get(defaultName);
    }

    private String getBakiNameRelatedMapper() {
        if (mapperInterface.isAnnotationPresent(Baki.class)) {
            String value = mapperInterface.getDeclaredAnnotation(Baki.class).value();
            if (!StringUtils.isBlank(value)) {
                return value;
            }
        }
        return "baki";
    }
}
