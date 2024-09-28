package com.github.chengyuxing.sql.spring.autoconfigure.mapping;

import com.github.chengyuxing.sql.annotation.XQLMapper;
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
    public XQLClasspathMapperScanner(BeanDefinitionRegistry registry) {
        super(registry, false);
        addIncludeFilter(new AnnotationTypeFilter(XQLMapper.class));
    }

    @Override
    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
        return true;
    }

    @Override
    protected Set<BeanDefinitionHolder> doScan(String... basePackages) {
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
        }
    }
}
