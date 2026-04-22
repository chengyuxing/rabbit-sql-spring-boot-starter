package com.github.chengyuxing.sql.spring.components.web;

import com.github.chengyuxing.sql.Baki;
import com.github.chengyuxing.sql.XQLInvocationHandler;
import com.github.chengyuxing.sql.plugins.QueryExecutor;

import java.util.Map;

public abstract class XQLService {
    private final Baki baki;

    public XQLService(Baki baki) {
        this.baki = baki;
    }

    protected abstract Map<String, Object> globalParams();

    public Object doQuery(String type, String sql, Map<String, Object> params) {
        // FIXME 这里QUERY_PATTERN源码应该可以换成 Pattern 来提高性能
        if (!sql.matches(XQLInvocationHandler.QUERY_PATTERN)) {
            throw new IllegalStateException(sql);
        }
        String sqlRef = "&" + sql;
        QueryExecutor qe = baki.query(sqlRef).args(params);
        switch (type) {
            case "list":
                return qe.rows();
            case "first":
                return qe.findFirstRow();
            case "page":
                return qe.pageable().collect();
            default:
                throw new UnsupportedOperationException(type);
        }
    }

    public int doDml(String sql, Map<String, Object> params) {
        String sqlRef = "&" + sql;
        return baki.execute(sqlRef, params).getInt(0);
    }
}
