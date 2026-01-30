package com.github.chengyuxing.sql.spring.properties;

import com.github.chengyuxing.common.script.pipe.IPipe;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.LinkedHashMap;
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
    private Map<String, String> files = new LinkedHashMap<>();
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
     * Set named param prefix for prepared SQL.
     */
    private char namedParamPrefix = ':';

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

    public char getNamedParamPrefix() {
        return namedParamPrefix;
    }

    public void setNamedParamPrefix(char namedParamPrefix) {
        this.namedParamPrefix = namedParamPrefix;
    }
}
