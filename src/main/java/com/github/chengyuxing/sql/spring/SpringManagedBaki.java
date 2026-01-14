package com.github.chengyuxing.sql.spring;

import com.github.chengyuxing.sql.BakiDao;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.jdbc.support.SQLExceptionTranslator;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Implementation of Baki for Spring, datasource and transaction managed by Spring.
 */
public class SpringManagedBaki extends BakiDao {
    private final SQLExceptionTranslator sqlExceptionTranslator;

    public SpringManagedBaki(DataSource dataSource) {
        super(dataSource);
        this.sqlExceptionTranslator = new SQLErrorCodeSQLExceptionTranslator(dataSource);
    }

    @Override
    protected @NotNull Connection getConnection() {
        return DataSourceUtils.getConnection(getDataSource());
    }

    @Override
    protected void releaseConnection(Connection connection, DataSource dataSource) {
        DataSourceUtils.releaseConnection(connection, dataSource);
    }

    @Override
    protected @NotNull RuntimeException wrappedDataAccessException(@Nullable String sql, @NotNull Throwable throwable) {
        SQLException sqlEx = findSqlException(throwable);
        if (sqlEx != null) {
            //noinspection SqlSourceToSinkFlow
            DataAccessException dae = sqlExceptionTranslator.translate("Rabbit-SQL", sql, sqlEx);
            if (dae != null) {
                return dae;
            }
        }
        return super.wrappedDataAccessException(sql, throwable);
    }

    private SQLException findSqlException(Throwable t) {
        while (t != null) {
            if (t instanceof SQLException) {
                return (SQLException) t;
            }
            t = t.getCause();
        }
        return null;
    }
}
