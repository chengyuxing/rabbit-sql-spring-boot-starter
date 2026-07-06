package com.github.chengyuxing.sql.spring.autoconfigure.mapping;

import com.github.chengyuxing.sql.BakiDao;
import com.github.chengyuxing.sql.XQLInvocationHandler;
import com.github.chengyuxing.sql.util.XQLMapperUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.function.Supplier;

public class XQLMapperFactoryBean<T> implements FactoryBean<T>, ApplicationContextAware {

    private final Class<T> mapperInterface;
    private final Supplier<BakiDao> bakiSupplier;
    private ApplicationContext applicationContext;

    public XQLMapperFactoryBean(Class<T> mapperClass) {
        this.mapperInterface = mapperClass;
        if (!mapperClass.isAnnotationPresent(Baki.class)) {
            bakiSupplier = () -> applicationContext.getBean(BakiDao.class);
        } else {
            String name = mapperClass.getDeclaredAnnotation(Baki.class).value();
            bakiSupplier = () -> applicationContext.getBean(name, BakiDao.class);
        }
    }

    @Override
    public T getObject() throws Exception {
        return XQLMapperUtils.getProxyInstance(mapperInterface, new XQLInvocationHandler() {
            @Override
            protected @NotNull BakiDao baki() {
                return bakiSupplier.get();
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
}
