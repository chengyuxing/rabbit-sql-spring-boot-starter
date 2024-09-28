package com.github.chengyuxing.sql.spring.autoconfigure.mapping;

import java.lang.annotation.*;

/**
 * Work in interface which annotated with {@link com.github.chengyuxing.sql.annotation.XQLMapper @XQLMapper},
 * the default {@link com.github.chengyuxing.sql.Baki Baki} bean of name 'baki' instance will be injected,
 * otherwise annotated with {@code @Baki} to specify the name (Especially if you have multiple
 * {@link com.github.chengyuxing.sql.Baki Baki} bean instances in
 * {@link org.springframework.context.ApplicationContext application context}).
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface Baki {
    /**
     * Baki bean name in {@link org.springframework.context.ApplicationContext application context}
     *
     * @return bean name
     */
    String value();
}
