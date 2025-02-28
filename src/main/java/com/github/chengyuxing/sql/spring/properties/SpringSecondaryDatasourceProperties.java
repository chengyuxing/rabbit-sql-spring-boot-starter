package com.github.chengyuxing.sql.spring.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

@ConfigurationProperties(prefix = "spring.datasource")
public class SpringSecondaryDatasourceProperties {
    /**
     * Configure multiple secondary datasource instances.
     */
    private Map<String, BakiDatasourceProperties> secondaries = new HashMap<>();

    public Map<String, BakiDatasourceProperties> getSecondaries() {
        return secondaries;
    }

    public void setSecondaries(Map<String, BakiDatasourceProperties> secondaries) {
        this.secondaries = secondaries;
    }
}
