package com.github.chengyuxing.sql.spring.properties;

import javax.sql.DataSource;
import java.util.Map;

/**
 * Baki datasource properties.
 */
public class BakiDatasourceProperties {
    /**
     * Datasource class name e.g. <code>com.zaxxer.hikari.HikariDataSource</code> .
     */
    private Class<? extends DataSource> className;
    /**
     * Datasource properties e.g. HikariDataSource's <code>jdbc-url</code> .
     */
    private Map<String, Object> properties;

    public Class<? extends DataSource> getClassName() {
        return className;
    }

    public void setClassName(Class<? extends DataSource> className) {
        this.className = className;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }
}
