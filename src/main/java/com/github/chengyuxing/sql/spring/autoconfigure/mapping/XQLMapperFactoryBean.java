package com.github.chengyuxing.sql.spring.autoconfigure.mapping;

import com.github.chengyuxing.sql.BakiDao;
import com.github.chengyuxing.sql.XQLInvocationHandler;
import com.github.chengyuxing.sql.utils.XQLMapperUtil;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Map;

public class XQLMapperFactoryBean<T> implements FactoryBean<T>, ApplicationContextAware {
    private final Class<T> mapperInterface;
    private ApplicationContext applicationContext;

    public XQLMapperFactoryBean(Class<T> mapperClass) {
        this.mapperInterface = mapperClass;
    }

    @Override
    public T getObject() throws Exception {
        return XQLMapperUtil.getProxyInstance(mapperInterface, new XQLInvocationHandler() {
            @Override
            protected BakiDao baki() {
                return getTargetBaki();
            }
        });
    }

    @Override
    public Class<?> getObjectType() {
        return mapperInterface;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    private BakiDao getTargetBaki() {
        Map<String, BakiDao> map = applicationContext.getBeansOfType(BakiDao.class);
        if (map.size() == 1) {
            return map.values().iterator().next();
        }
        String defaultName = getBakiNameRelatedMapper();
        return map.get(defaultName);
    }

    private String getBakiNameRelatedMapper() {
        if (mapperInterface.isAnnotationPresent(Baki.class)) {
            String value = mapperInterface.getDeclaredAnnotation(Baki.class).value();
            if (!value.trim().isEmpty()) {
                return value;
            }
        }
        return "baki";
    }
}
