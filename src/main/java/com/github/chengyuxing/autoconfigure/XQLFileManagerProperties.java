package com.github.chengyuxing.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * 支持扩展脚本解析动态SQL的文件管理器
 *
 * @see com.github.chengyuxing.sql.XQLFileManager
 */
@ConfigurationProperties(prefix = "baki.xql-file-manager")
public class XQLFileManagerProperties {
    /**
     * 命名的sql文件 [别名，文件名]
     */
    private Map<String, String> files = new HashMap<>();
    /**
     * 设置全局常量集合<br>
     * 初始化扫描sql时，如果sql文件中没有找到匹配的字符串模版，则从全局常量中寻找
     * 格式为：
     * <blockquote>
     * <pre>constants: {db:"test"}</pre>
     * <pre>sql: {@code select ${db}.user from table;}</pre>
     * <pre>result: select test.user from table.</pre>
     * </blockquote>
     */
    private Map<String, String> constants = new HashMap<>();
    /**
     * 配置动态sql脚本自定义管道字典 [管道名，IPipe实现类全名]
     */
    private Map<String, String> pipes = new HashMap<>();
    /**
     * 文件检查周期（单位：秒）
     */
    private int checkPeriod = 30; //seconds
    /**
     * 检查sql文件是否更新
     */
    private boolean checkModified = false;
    /**
     * 解析sql文件使用的编码格式，默认为UTF-8
     */
    private String charset = "UTF-8";
    /**
     * 每个文件的sql片段块解析分隔符，每一段完整的sql根据此设置来进行区分，
     * 默认是单个分号（{@code ;}）遵循标准sql文件多段sql分隔符。<br>但是有一种情况，如果sql文件内有<b>psql</b>：{@code create function...} 或 {@code create procedure...}等，
     * 内部会包含多段sql多个分号，为防止解析异常，单独设置自定义的分隔符：
     * <ul>
     *     <li>例如（{@code ;;}）双分号，也是标准sql所支持的, <b>并且支持仅扫描已命名的sql</b>；</li>
     *     <li>也可以设置为null或空白，那么整个SQL文件多段SQL都应按照此方式分隔。</li>
     * </ul>
     */
    private String delimiter = ";";
    /**
     * 命名参数前缀，主要处理动态sql {@code for} 表达式中形如：{@code ${:name}} 这样的情况
     */
    private char namedParamPrefix = ':';

    public Map<String, String> getFiles() {
        return files;
    }

    public void setFiles(Map<String, String> files) {
        this.files = files;
    }

    public Map<String, String> getConstants() {
        return constants;
    }

    public void setConstants(Map<String, String> constants) {
        this.constants = constants;
    }

    public Map<String, String> getPipes() {
        return pipes;
    }

    public void setPipes(Map<String, String> pipes) {
        this.pipes = pipes;
    }

    public int getCheckPeriod() {
        return checkPeriod;
    }

    public void setCheckPeriod(int checkPeriod) {
        this.checkPeriod = checkPeriod;
    }

    public boolean isCheckModified() {
        return checkModified;
    }

    public void setCheckModified(boolean checkModified) {
        this.checkModified = checkModified;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public String getDelimiter() {
        return delimiter;
    }

    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    public char getNamedParamPrefix() {
        return namedParamPrefix;
    }

    public void setNamedParamPrefix(char namedParamPrefix) {
        this.namedParamPrefix = namedParamPrefix;
    }
}
