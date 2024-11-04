package com.github.chengyuxing.sql.spring.autoconfigure.mapping;

import com.github.chengyuxing.sql.annotation.XQLMapper;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.util.Objects;
import java.util.Set;

public class XQLClasspathMapperScanner extends ClassPathBeanDefinitionScanner {
    private static final Logger log = LoggerFactory.getLogger(XQLClasspathMapperScanner.class);

    public XQLClasspathMapperScanner(BeanDefinitionRegistry registry) {
        super(registry, false);
        addIncludeFilter(new AnnotationTypeFilter(XQLMapper.class));
    }

    @Override
    protected boolean isCandidateComponent(@NotNull AnnotatedBeanDefinition beanDefinition) {
        return true;
    }

    @Override
    protected @NotNull Set<BeanDefinitionHolder> doScan(String @NotNull ... basePackages) {
        Set<BeanDefinitionHolder> beanDefinitions = super.doScan(basePackages);
        if (!beanDefinitions.isEmpty()) {
            processBeanDefinitions(beanDefinitions);
        }
        return beanDefinitions;
    }

    private void processBeanDefinitions(Set<BeanDefinitionHolder> beanDefinitions) {
        for (BeanDefinitionHolder holder : beanDefinitions) {
            GenericBeanDefinition definition = (GenericBeanDefinition) holder.getBeanDefinition();
            String beanClassName = definition.getBeanClassName();
            if (Objects.isNull(beanClassName)) {
                continue;
            }
            definition.setBeanClass(XQLMapperFactoryBean.class);
            definition.getConstructorArgumentValues().addGenericArgumentValue(beanClassName);

            definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);

            log.debug("Scan and register XQL mapper: {}.", beanClassName);
        }
    }
}
