package com.github.chengyuxing.sql.spring.autoconfigure;

import com.github.chengyuxing.sql.page.PageHelperProvider;
import com.github.chengyuxing.sql.support.AfterParseDynamicSql;
import com.github.chengyuxing.sql.support.SqlInterceptor;
import com.github.chengyuxing.sql.support.StatementValueHandler;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Baki configuration properties.
 *
 * @see SpringManagedBaki
 */
@ConfigurationProperties(prefix = "baki")
public class BakiProperties {
    /**
     * XQLFileManager properties.
     */
    private XQLFileManagerProperties xqlFileManager;
    /**
     * Global page helper provider.
     */
    private Class<? extends PageHelperProvider> globalPageHelperProvider;
    /**
     * SQL interceptor.
     */
    private Class<? extends SqlInterceptor> sqlInterceptor;
    /**
     * Custom prepared sql statement parameter value handler.
     */
    private Class<? extends StatementValueHandler> statementValueHandler;
    /**
     * Do something after parse dynamic sql.
     */
    private Class<? extends AfterParseDynamicSql> afterParseDynamicSql;
    /**
     * Batch size of batch execute.
     */
    private int batchSize = 1000;
    /**
     * Named parameter prefix.
     */
    private char namedParamPrefix = ':';
    /**
     * If XQL file changed, XQL file reloaded when execute sql always.
     */
    private boolean reloadXqlOnGet = false;
    /**
     * Load {@code xql-file-manager-}{@link SpringManagedBaki#databaseId() databaseId}{@code .yml} first if exists,
     * otherwise {@code xql-file-manager.yml}
     */
    private boolean autoXFMConfig = true;

    public XQLFileManagerProperties getXqlFileManager() {
        return xqlFileManager;
    }

    public void setXqlFileManager(XQLFileManagerProperties xqlFileManager) {
        this.xqlFileManager = xqlFileManager;
    }

    public char getNamedParamPrefix() {
        return namedParamPrefix;
    }

    public void setNamedParamPrefix(char namedParamPrefix) {
        this.namedParamPrefix = namedParamPrefix;
    }

    public Class<? extends PageHelperProvider> getGlobalPageHelperProvider() {
        return globalPageHelperProvider;
    }

    public void setGlobalPageHelperProvider(Class<? extends PageHelperProvider> globalPageHelperProvider) {
        this.globalPageHelperProvider = globalPageHelperProvider;
    }

    public Class<? extends SqlInterceptor> getSqlInterceptor() {
        return sqlInterceptor;
    }

    public void setSqlInterceptor(Class<? extends SqlInterceptor> sqlInterceptor) {
        this.sqlInterceptor = sqlInterceptor;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public boolean isReloadXqlOnGet() {
        return reloadXqlOnGet;
    }

    public void setReloadXqlOnGet(boolean reloadXqlOnGet) {
        this.reloadXqlOnGet = reloadXqlOnGet;
    }

    public Class<? extends StatementValueHandler> getStatementValueHandler() {
        return statementValueHandler;
    }

    public void setStatementValueHandler(Class<? extends StatementValueHandler> statementValueHandler) {
        this.statementValueHandler = statementValueHandler;
    }

    public Class<? extends AfterParseDynamicSql> getAfterParseDynamicSql() {
        return afterParseDynamicSql;
    }

    public void setAfterParseDynamicSql(Class<? extends AfterParseDynamicSql> afterParseDynamicSql) {
        this.afterParseDynamicSql = afterParseDynamicSql;
    }

    public boolean isAutoXFMConfig() {
        return autoXFMConfig;
    }

    public void setAutoXFMConfig(boolean autoXFMConfig) {
        this.autoXFMConfig = autoXFMConfig;
    }
}
