package com.github.chengyuxing.sql.spring;

import com.github.chengyuxing.sql.BakiDao;
import com.github.chengyuxing.sql.plugins.NamedParamFormatter;
import com.github.chengyuxing.sql.plugins.TemplateFormatter;
import com.github.chengyuxing.sql.utils.SqlGenerator;
import org.jetbrains.annotations.NotNull;
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
    protected @NotNull Connection getConnection() {
        return DataSourceUtils.getConnection(getDataSource());
    }

    @Override
    protected void releaseConnection(Connection connection, DataSource dataSource) {
        DataSourceUtils.releaseConnection(connection, dataSource);
    }

    public void setTemplateFormatter(TemplateFormatter templateFormatter) {
        SqlGenerator sqlGenerator = getSqlGenerator();
        sqlGenerator.setTemplateFormatter(templateFormatter);
    }

    public void setNamedParamFormatter(NamedParamFormatter namedParamFormatter) {
        SqlGenerator sqlGenerator = getSqlGenerator();
        sqlGenerator.setNamedParamFormatter(namedParamFormatter);
    }
}
