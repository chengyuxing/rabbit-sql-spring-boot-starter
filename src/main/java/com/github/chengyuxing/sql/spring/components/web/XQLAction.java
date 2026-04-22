package com.github.chengyuxing.sql.spring.components.web;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

public abstract class XQLAction<T> {
    private final XQLService xqlService;

    public XQLAction(XQLService xqlService) {
        this.xqlService = xqlService;
    }

    protected abstract T resultWrap(Object result);

    @PostMapping("/{target}/{type}")
    public T query(@PathVariable String target, @PathVariable String type, @RequestBody Map<String, Object> params) {
        Object result = this.xqlService.doQuery(type, target, params);
        return resultWrap(result);
    }
}
