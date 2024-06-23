package com.github.chengyuxing.sql.spring.autoconfigure;

import com.github.chengyuxing.common.script.expression.IPipe;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Dynamic SQL parse file manager.
 *
 * @see com.github.chengyuxing.sql.XQLFileManager
 */
@ConfigurationProperties(prefix = "baki.xql-file-manager")
public class XQLFileManagerProperties {
    /**
     * Config location, support {@code yml} and {@code properties}.
     */
    private String configLocation;
    /**
     * Named sql file [alias, file name].
     */
    private Map<String, String> files = new HashMap<>();
    /**
     * Set constants map.<br>
     * Example：
     * <p>constants: </p>
     * <blockquote>
     * <pre>{db: "test"}</pre>
     * </blockquote>
     * <p>sql statement:</p>
     * <blockquote>
     * <pre>select ${db}.user from table</pre>
     * </blockquote>
     * <p>result: </p>
     * <blockquote>
     * <pre>select test.user from table</pre>
     * </blockquote>
     */
    private Map<String, Object> constants = new HashMap<>();
    /**
     * Set custom pipe class name map[pipe name，pipe class].
     */
    @SuppressWarnings("rawtypes")
    private Map<String, Class<? extends IPipe>> pipes = new HashMap<>();
    /**
     * Set sql file parsing charset, UTF-8 is default.
     */
    private String charset = "UTF-8";
    /**
     * Set delimiter of multi sql fragment/template, symbol ({@code ;}) is default.<br>
     * Sometimes default delimiter is not enough, such as one procedure body or plsql maybe contains
     * more than one sql statement which ends with {@code ;}, for correct set to other is necessary, like {@code ;;} .
     */
    private String delimiter = ";";
    /**
     * Current database id.<br>
     * Init value for support <a href="https://plugins.jetbrains.com/plugin/21403-rabbit-sql">Rabbit-sql-plugin</a> dynamic sql test parameter
     * and assigned in runtime automatically.
     */
    private String databaseId;

    public Map<String, String> getFiles() {
        return files;
    }

    public void setFiles(Map<String, String> files) {
        this.files = files;
    }

    public Map<String, Object> getConstants() {
        return constants;
    }

    public void setConstants(Map<String, Object> constants) {
        this.constants = constants;
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

    @SuppressWarnings("rawtypes")
    public Map<String, Class<? extends IPipe>> getPipes() {
        return pipes;
    }

    @SuppressWarnings("rawtypes")
    public void setPipes(Map<String, Class<? extends IPipe>> pipes) {
        this.pipes = pipes;
    }

    public String getConfigLocation() {
        return configLocation;
    }

    public void setConfigLocation(String configLocation) {
        this.configLocation = configLocation;
    }

    public String getDatabaseId() {
        return databaseId;
    }

    public void setDatabaseId(String databaseId) {
        this.databaseId = databaseId;
    }
}
