package com.github.chengyuxing.sql.spring.autoconfigure;

import com.github.chengyuxing.sql.page.PageHelperProvider;
import com.github.chengyuxing.sql.support.SqlInterceptor;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Baki配置项
 *
 * @see SpringManagedBaki
 */
@ConfigurationProperties(prefix = "baki")
public class BakiProperties {
    /**
     * 支持扩展脚本解析动态SQL的文件管理器
     */
    private XQLFileManagerProperties xqlFileManager;
    /**
     * 自定义的全局分页帮助提供程序
     */
    private Class<? extends PageHelperProvider> globalPageHelperProvider;
    /**
     * SQL拦截器
     */
    private Class<? extends SqlInterceptor> sqlInterceptor;
    /**
     * 全局命名参数前缀，默认为 : 号
     */
    private char namedParamPrefix = ':';
    /**
     * 是否检查预编译sql对应的参数类型，取决于jdbc驱动厂商是否支持
     *
     * @deprecated
     */
    @Deprecated
    private boolean checkParameterType = true;

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

    public boolean isCheckParameterType() {
        return checkParameterType;
    }

    public void setCheckParameterType(boolean checkParameterType) {
        this.checkParameterType = checkParameterType;
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
}
