package com.github.chengyuxing.sql.spring.properties;

import com.github.chengyuxing.sql.spring.SpringManagedBaki;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Primary Baki configuration properties.
 *
 * @see SpringManagedBaki
 */
@ConfigurationProperties(prefix = "baki")
public class PrimaryBakiProperties extends BakiProperties {
    /**
     * Configure multiple secondary baki instances.
     */
    private Map<String, BakiProperties> secondaries = new HashMap<>();

    public Map<String, BakiProperties> getSecondaries() {
        return secondaries;
    }

    public void setSecondaries(Map<String, BakiProperties> secondaries) {
        this.secondaries = secondaries;
    }
}
