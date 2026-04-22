package com.github.chengyuxing.sql.spring.components.cache;

import com.github.chengyuxing.common.DataRow;
import org.jetbrains.annotations.Range;

import java.io.Serializable;
import java.util.List;

public final class CacheEntry implements Serializable {
    private static final long serialVersionUID = 1L;
    List<DataRow> value;
    long softExpireAt = 0;
    long hardExpireAt = 0;

    public void setSoftTimeout(@Range(from = 0, to = Long.MAX_VALUE) long milliseconds) {
        this.softExpireAt = System.currentTimeMillis() + milliseconds;
    }

    public void setHardTimeout(@Range(from = 0, to = Long.MAX_VALUE) long milliseconds) {
        this.hardExpireAt = System.currentTimeMillis() + milliseconds;
    }
}
