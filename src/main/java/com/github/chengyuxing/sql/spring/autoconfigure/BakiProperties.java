package com.github.chengyuxing.sql.spring.autoconfigure;

import com.github.chengyuxing.sql.page.PageHelper;
import com.github.chengyuxing.sql.page.PageHelperProvider;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

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
     * 全局命名参数前缀，默认为 : 号
     */
    private char namedParamPrefix = ':';
    /**
     * 动态sql的参数是否为严格模式
     */
    private boolean strictDynamicSqlArg = true;
    /**
     * 是否检查预编译sql对应的参数类型，取决于jdbc驱动厂商是否支持
     */
    private boolean checkParameterType = true;
    /**
     * 是否打印拼接完整的SQL，否则只打印原始SQL与参数
     */
    private boolean debugFullSql = false;
    /**
     * debug模式下是否打印语法高亮sql，默认非高亮
     */
    private boolean highlightSql = false;

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

    public boolean isStrictDynamicSqlArg() {
        return strictDynamicSqlArg;
    }

    public void setStrictDynamicSqlArg(boolean strictDynamicSqlArg) {
        this.strictDynamicSqlArg = strictDynamicSqlArg;
    }

    public boolean isCheckParameterType() {
        return checkParameterType;
    }

    public void setCheckParameterType(boolean checkParameterType) {
        this.checkParameterType = checkParameterType;
    }

    public boolean isDebugFullSql() {
        return debugFullSql;
    }

    public void setDebugFullSql(boolean debugFullSql) {
        this.debugFullSql = debugFullSql;
    }

    public boolean isHighlightSql() {
        return highlightSql;
    }

    public void setHighlightSql(boolean highlightSql) {
        this.highlightSql = highlightSql;
    }

    public Class<? extends PageHelperProvider> getGlobalPageHelperProvider() {
        return globalPageHelperProvider;
    }

    public void setGlobalPageHelperProvider(Class<? extends PageHelperProvider> globalPageHelperProvider) {
        this.globalPageHelperProvider = globalPageHelperProvider;
    }
}
