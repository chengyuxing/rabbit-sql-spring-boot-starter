package com.github.chengyuxing.sql.spring.properties;

import com.github.chengyuxing.sql.plugins.*;
import com.github.chengyuxing.sql.utils.SqlUtil;

/**
 * Baki configuration properties.
 */
public class BakiProperties {
    /**
     * Datasource bean name (secondary datasource will be injected by name to secondary baki).
     */
    private String datasource;
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
    private Class<? extends SqlParseChecker> sqlParseChecker;
    /**
     * Batch size of batch execute.
     */
    private int batchSize = 1000;
    /**
     * Named parameter prefix.
     */
    private char namedParamPrefix = ':';
    /**
     * Non-prepared Sql template ({@code ${key}}) formatter.
     * Default implementation: {@link SqlUtil#parseValue(Object, boolean) parseValue(value, boolean)}
     */
    private Class<? extends TemplateFormatter> templateFormatter;
    /**
     * Non-prepared Sql named parameter value formatter.
     * Default implementation: {@link SqlUtil#parseValue(Object, boolean) parseValue(value, true)}
     */
    private Class<? extends NamedParamFormatter> namedParamFormatter;
    /**
     * Page query page number argument key.
     */
    private String pageKey = "page";
    /**
     * Page query page size argument key.
     */
    private String sizeKey = "size";
    /**
     * Jdbc execute sql timeout.
     */
    private Class<? extends QueryTimeoutHandler> queryTimeoutHandler;
    /**
     * Sql watcher.
     */
    private Class<? extends ExecutionWatcher> executionWatcher;
    /**
     * Query cache manager.
     */
    private Class<? extends QueryCacheManager> queryCacheManager;
    /**
     * Default Map to Entity field mapper support.
     */
    private Class<? extends EntityFieldMapper> entityFieldMapper;
    /**
     * Default Map to Entity value mapper support.
     */
    private Class<? extends EntityValueMapper> entityValueMapper;

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

    public Class<? extends StatementValueHandler> getStatementValueHandler() {
        return statementValueHandler;
    }

    public void setStatementValueHandler(Class<? extends StatementValueHandler> statementValueHandler) {
        this.statementValueHandler = statementValueHandler;
    }

    public Class<? extends TemplateFormatter> getTemplateFormatter() {
        return templateFormatter;
    }

    public void setTemplateFormatter(Class<? extends TemplateFormatter> templateFormatter) {
        this.templateFormatter = templateFormatter;
    }

    public Class<? extends NamedParamFormatter> getNamedParamFormatter() {
        return namedParamFormatter;
    }

    public void setNamedParamFormatter(Class<? extends NamedParamFormatter> namedParamFormatter) {
        this.namedParamFormatter = namedParamFormatter;
    }

    public String getPageKey() {
        return pageKey;
    }

    public void setPageKey(String pageKey) {
        this.pageKey = pageKey;
    }

    public String getSizeKey() {
        return sizeKey;
    }

    public void setSizeKey(String sizeKey) {
        this.sizeKey = sizeKey;
    }

    public Class<? extends QueryTimeoutHandler> getQueryTimeoutHandler() {
        return queryTimeoutHandler;
    }

    public void setQueryTimeoutHandler(Class<? extends QueryTimeoutHandler> queryTimeoutHandler) {
        this.queryTimeoutHandler = queryTimeoutHandler;
    }

    public Class<? extends QueryCacheManager> getQueryCacheManager() {
        return queryCacheManager;
    }

    public void setQueryCacheManager(Class<? extends QueryCacheManager> queryCacheManager) {
        this.queryCacheManager = queryCacheManager;
    }

    public Class<? extends SqlParseChecker> getSqlParseChecker() {
        return sqlParseChecker;
    }

    public void setSqlParseChecker(Class<? extends SqlParseChecker> sqlParseChecker) {
        this.sqlParseChecker = sqlParseChecker;
    }

    public String getDatasource() {
        return datasource;
    }

    public void setDatasource(String datasource) {
        this.datasource = datasource;
    }

    public Class<? extends ExecutionWatcher> getExecutionWatcher() {
        return executionWatcher;
    }

    public void setExecutionWatcher(Class<? extends ExecutionWatcher> executionWatcher) {
        this.executionWatcher = executionWatcher;
    }

    public Class<? extends EntityFieldMapper> getEntityFieldMapper() {
        return entityFieldMapper;
    }

    public void setEntityFieldMapper(Class<? extends EntityFieldMapper> entityFieldMapper) {
        this.entityFieldMapper = entityFieldMapper;
    }

    public Class<? extends EntityValueMapper> getEntityValueMapper() {
        return entityValueMapper;
    }

    public void setEntityValueMapper(Class<? extends EntityValueMapper> entityValueMapper) {
        this.entityValueMapper = entityValueMapper;
    }
}
