package com.github.chengyuxing.spring.autoconfigure;

import com.github.chengyuxing.sql.BakiDao;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * spring管理事务的Baki
 */
public class SpringManagedBaki extends BakiDao {
    /**
     * 构造函数
     *
     * @param dataSource 数据源
     */
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
