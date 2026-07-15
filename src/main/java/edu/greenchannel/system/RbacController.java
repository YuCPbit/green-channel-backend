package edu.greenchannel.system;

import edu.greenchannel.auth.RequirePermission;
import edu.greenchannel.common.ApiResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/system/rbac")
public class RbacController {
    private final JdbcTemplate jdbcTemplate;

    public RbacController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/overview")
    @RequirePermission("system:rbac:view")
    public ApiResponse<Map<String, Long>> overview() {
        Long users = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM gc_user WHERE is_deleted = 0", Long.class);
        Long roles = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM gc_role WHERE is_deleted = 0", Long.class);
        Long permissions = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM gc_permission WHERE is_deleted = 0", Long.class);
        return ApiResponse.success(Map.of("users", users, "roles", roles, "permissions", permissions));
    }
}
