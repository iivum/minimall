package com.minimall.controller;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/health")
public class HealthController {

    @Autowired
    private MeterRegistry meterRegistry;

    @Autowired
    private DataSource dataSource;

    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", System.currentTimeMillis());

        Map<String, Object> components = new HashMap<>();

        // HikariCP connection pool metrics
        if (dataSource != null) {
            try {
                String poolName = dataSource.getClass().getSimpleName();
                components.put("dataSource", Map.of(
                    "type", poolName,
                    "status", "UP"
                ));

                // Get HikariCP metrics if available
                if (meterRegistry instanceof SimpleMeterRegistry) {
                    var hikariMetrics = meterRegistry.find("hikaricp").meterIds();
                    if (!hikariMetrics.isEmpty()) {
                        components.put("hikariCP", Map.of(
                            "metrics", "available",
                            "status", "UP"
                        ));
                    }
                }

                // Test connection
                try (Connection conn = dataSource.getConnection()) {
                    components.put("database", Map.of(
                        "connection", "acquirable",
                        "status", "UP"
                    ));
                }
            } catch (Exception e) {
                components.put("database", Map.of(
                    "error", e.getMessage(),
                    "status", "DOWN"
                ));
            }
        }

        response.put("components", components);
        return ResponseEntity.ok(response);
    }
}
