package com.github.chengyuxing.sql.spring.autoconfigure.mapping;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Arrays;
import java.util.Objects;

public class XQLMapperScannerRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware, EnvironmentAware {
    private static final Logger log = LoggerFactory.getLogger(XQLMapperScannerRegistrar.class);

    private ResourceLoader resourceLoader;
    private Environment environment;

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, @NotNull BeanDefinitionRegistry registry) {
        AnnotationAttributes attributes = AnnotationAttributes.fromMap(
                importingClassMetadata.getAnnotationAttributes(XQLMapperScan.class.getName())
        );
        if (Objects.isNull(attributes)) {
            return;
        }
        String[] basePackages = attributes.getStringArray("basePackages");
        if (basePackages.length == 0) {
            basePackages = new String[]{getDefaultBasePackage(importingClassMetadata)};
        }
        log.debug("@XQLMapperScan: scanning packages: {}.", Arrays.toString(basePackages));
        XQLClasspathMapperScanner scanner = new XQLClasspathMapperScanner(registry);
        scanner.setResourceLoader(this.resourceLoader);
        scanner.setEnvironment(this.environment);
        scanner.scan(basePackages);
    }

    private String getDefaultBasePackage(AnnotationMetadata importingClassMetadata) {
        // startup main class package
        String className = importingClassMetadata.getClassName();
        int lastDotIndex = className.lastIndexOf('.');
        return (lastDotIndex != -1) ? className.substring(0, lastDotIndex) : "";
    }

    @Override
    public void setEnvironment(@NotNull Environment environment) {
        this.environment = environment;
    }

    @Override
    public void setResourceLoader(@NotNull ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }
}
