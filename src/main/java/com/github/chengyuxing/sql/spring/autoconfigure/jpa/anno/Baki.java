package com.github.chengyuxing.sql.spring.autoconfigure.jpa.anno;

import org.springframework.data.jpa.repository.Query;

import java.lang.annotation.*;
import java.util.Map;

/**
 * 用于扩展jpa执行外部sql的Baki注解，配合注解 {@linkplain  Query @Query} 一起工作
 * <blockquote>
 * e.g.
 * <pre>
 * {@linkplain org.springframework.data.repository.Repository @Repository}
 * {@code public interface MyDao extends JpaRepository<Users, Integer>} {
 *     {@linkplain Baki @Baki}
 *     {@linkplain Query @Query}(value = {@code "&test.users"}, countQuery = {@code "&test.users.count"}, nativeQuery = {@code true})
 *     {@linkplain com.github.chengyuxing.sql.PagedResource PagedResource}&lt;{@linkplain com.github.chengyuxing.common.DataRow DataRow}&gt; getNow(Map&lt;String, Object&gt; args);
 * }</pre><br>
 * <p>
 * 关于 {@linkplain  Query @Query} 需要遵循几个规则：
 * <ul>
 *     <li>属性 {@linkplain Query#nativeQuery() nativeQuery} 必须设置为 {@code true}</li>
 *     <li>属性 {@linkplain Query#value() value} 为sql名，e.g. {@code &my.query}</li>
 *     <li>属性[可选] {@linkplain Query#countQuery() countQuery} 为分页查询的条数查询sql名</li>
 * </ul>
 * 标注此注解的方法参数值遵循以下原则：
 * <ul>
 *     <li>参数个数有且只有一个并且类型为 {@link Map} 则这就是sql中所有的参数占位符为key字典集合</li>
 *     <li>参数个数大于一个，则每个参数对应一个sql参数占位符</li>
 *     <li>分页查询中，默认的 当前页 和 每页大小 的参数名分别为 page 和 size</li>
 * </ul>
 * </blockquote>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface Baki {
    /**
     * 执行类型
     *
     * @return 执行类型枚举，默认为 查询
     */
    Type value() default Type.QUERY;

    /**
     * 针对分页查询生效的属性，禁用默认的自动包裹分页sql
     *
     * @return 是否禁用，默认 false
     * @see com.github.chengyuxing.sql.page.IPageable#disableDefaultPageSql(String...)
     */
    boolean disableDefaultPageSql() default false;

    enum Type {
        /**
         * 查询<br>
         * <p>
         * 返回值类型支持：{@linkplain  java.util.stream.Stream Stream}、
         * {@linkplain java.util.List List}、
         * {@linkplain java.util.Set Set}、
         * {@linkplain java.util.Optional Optional}、
         * {@linkplain com.github.chengyuxing.common.DataRow DataRow | Map}、
         * {@linkplain com.github.chengyuxing.sql.PagedResource PagedResource}<br>
         * 其中泛型参数类型支持 {@linkplain com.github.chengyuxing.common.DataRow DataRow | Map} 和标准 javaBean
         *
         * @see com.github.chengyuxing.sql.Baki#query(String) Baki.query
         */
        QUERY,
        /**
         * 执行存储过程或函数<br>
         * 返回值类型为 {@linkplain com.github.chengyuxing.common.DataRow DataRow | Map}
         *
         * @see com.github.chengyuxing.sql.Baki#call(String, Map)
         */
        CALL,
        /**
         * 执行一个修改操作（DDL、DML）<br>
         * 返回值类型为 {@linkplain com.github.chengyuxing.common.DataRow DataRow | Map}
         *
         * @see com.github.chengyuxing.sql.Baki#execute(String, Map)
         */
        MODIFY
    }
}
