package com.github.chengyuxing.sql.spring.autoconfigure.mapping;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Annotated on Springboot application startup main class, it will scan the
 * interfaces which annotated with {@link com.github.chengyuxing.sql.annotation.XQLMapper @XQLMapper}
 * register to {@link org.springframework.context.ApplicationContext application context} .
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(XQLMapperScannerRegistrar.class)
public @interface XQLMapperScan {
    /**
     * Base packages to scan.
     */
    String[] basePackages() default {};
}
