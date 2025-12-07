package com.github.chengyuxing.sql.spring.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Baki configuration properties.
 */
@ConfigurationProperties(prefix = "baki")
public class BakiProperties {
    /**
     * XQLFileManager properties.
     */
    private XQLFileManagerProperties xqlFileManager;
    /**
     * Batch size of batch execute.
     */
    private int batchSize = 1000;
    /**
     * Named parameter prefix.
     */
    private char namedParamPrefix = ':';
    /**
     * Page query page number argument key.
     */
    private String pageKey = "page";
    /**
     * Page query page size argument key.
     */
    private String sizeKey = "size";

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

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
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
}
