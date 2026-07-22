package edu.greenchannel.system;

import edu.greenchannel.common.ApiResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {
    private final JdbcTemplate jdbcTemplate;

    public HealthController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping
    public ApiResponse<Map<String, Object>> health() {
        Integer database = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
        return ApiResponse.success(Map.of("application", "UP", "database", database != null ? "UP" : "DOWN"));
    }
}

