package com.minimall.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "customer-service.auto-reply")
public class CustomerServiceConfig {
    private Map<String, String> rules = new HashMap<>();
    private boolean enabled = true;

    public Map<String, String> getRules() { return rules; }
    public void setRules(Map<String, String> rules) { this.rules = rules; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}