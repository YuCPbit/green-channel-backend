package edu.greenchannel.system;

import edu.greenchannel.auth.RequirePermission;
import edu.greenchannel.common.ApiResponse;
import edu.greenchannel.common.PageResult;
import edu.greenchannel.operationlog.OperationLog;
import edu.greenchannel.system.SystemAdminService.AdminUserRequest;
import edu.greenchannel.system.SystemAdminService.AdminUserView;
import edu.greenchannel.system.SystemAdminService.PasswordResetRequest;
import edu.greenchannel.system.SystemAdminService.PermissionView;
import edu.greenchannel.system.SystemAdminService.RoleRequest;
import edu.greenchannel.system.SystemAdminService.RoleView;
import edu.greenchannel.system.SystemAdminService.SystemConfigRequest;
import edu.greenchannel.system.SystemAdminService.SystemConfigView;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/system")
public class SystemAdminController {
    private final SystemAdminService service;

    public SystemAdminController(SystemAdminService service) {
        this.service = service;
    }

    @GetMapping("/users")
    @RequirePermission("system:user:view")
    public ApiResponse<PageResult<AdminUserView>> users(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer userType,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(service.users(keyword, userType, status, page, size));
    }

    @PostMapping("/users")
    @RequirePermission("system:user:edit")
    @OperationLog(module = "用户管理", action = "CREATE", targetId = "#result.data.id")
    public ApiResponse<AdminUserView> createUser(@RequestBody AdminUserRequest request) {
        return ApiResponse.success(service.createUser(request));
    }

    @PutMapping("/users/{id}")
    @RequirePermission("system:user:edit")
    @OperationLog(module = "用户管理", action = "UPDATE", targetId = "#id")
    public ApiResponse<AdminUserView> updateUser(@PathVariable long id, @RequestBody AdminUserRequest request) {
        return ApiResponse.success(service.updateUser(id, request));
    }

    @PostMapping("/users/{id}/reset-password")
    @RequirePermission("system:user:edit")
    @OperationLog(module = "用户管理", action = "RESET_PASSWORD", targetId = "#id")
    public ApiResponse<Void> resetPassword(@PathVariable long id, @RequestBody PasswordResetRequest request) {
        service.resetPassword(id, request);
        return ApiResponse.success(null);
    }

    @GetMapping("/roles")
    @RequirePermission("system:rbac:view")
    public ApiResponse<List<RoleView>> roles() {
        return ApiResponse.success(service.roles());
    }

    @PostMapping("/roles")
    @RequirePermission("system:rbac:edit")
    @OperationLog(module = "角色权限", action = "CREATE_ROLE", targetId = "#result.data.id")
    public ApiResponse<RoleView> createRole(@RequestBody RoleRequest request) {
        return ApiResponse.success(service.createRole(request));
    }

    @PutMapping("/roles/{id}")
    @RequirePermission("system:rbac:edit")
    @OperationLog(module = "角色权限", action = "UPDATE_ROLE", targetId = "#id")
    public ApiResponse<RoleView> updateRole(@PathVariable long id, @RequestBody RoleRequest request) {
        return ApiResponse.success(service.updateRole(id, request));
    }

    @GetMapping("/permissions")
    @RequirePermission("system:rbac:view")
    public ApiResponse<List<PermissionView>> permissions() {
        return ApiResponse.success(service.permissions());
    }

    @GetMapping("/configs")
    @RequirePermission("system:dictionary:view")
    public ApiResponse<List<SystemConfigView>> configs() {
        return ApiResponse.success(service.configs());
    }

    @PutMapping("/configs/{id}")
    @RequirePermission("system:dictionary:edit")
    @OperationLog(module = "系统参数", action = "UPDATE", targetId = "#id")
    public ApiResponse<SystemConfigView> updateConfig(
            @PathVariable long id, @RequestBody SystemConfigRequest request) {
        return ApiResponse.success(service.updateConfig(id, request));
    }
}
