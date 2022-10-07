package com.github.chengyuxing.autoconfigure;

import com.github.chengyuxing.sql.page.PageHelper;
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
     * 分页帮助类，如果默认数据库的实现不满足可配置此项，[{@code DatabaseMetaData#getDatabaseProductName()数据库名} ，分页类]
     */
    private Map<String, Class<? extends PageHelper>> pageHelpers = new HashMap<>();
    /**
     * 支持扩展脚本解析动态SQL的文件管理器
     */
    private XQLFileManagerProperties xqlFileManager;
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

    public Map<String, Class<? extends PageHelper>> getPageHelpers() {
        return pageHelpers;
    }

    public void setPageHelpers(Map<String, Class<? extends PageHelper>> pageHelpers) {
        this.pageHelpers = pageHelpers;
    }

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
}
