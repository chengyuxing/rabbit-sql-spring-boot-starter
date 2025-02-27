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
    private Map<String, BakiProperties> secondary = new HashMap<>();


    public Map<String, BakiProperties> getSecondary() {
        return secondary;
    }

    public void setSecondary(Map<String, BakiProperties> secondary) {
        this.secondary = secondary;
    }
}
