package com.github.chengyuxing.sql.spring.autoconfigure;

import com.github.chengyuxing.sql.BakiDao;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * Implementation of Baki for Spring, datasource and transaction managed by Spring.
 */
public class SpringManagedBaki extends BakiDao {
    public SpringManagedBaki(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    protected Connection getConnection() {
        return DataSourceUtils.getConnection(getDataSource());
    }

    @Override
    protected void releaseConnection(Connection connection, DataSource dataSource) {
        DataSourceUtils.releaseConnection(connection, dataSource);
    }
}
