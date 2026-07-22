package edu.greenchannel.dashboard.controller;

import edu.greenchannel.auth.RequirePermission;
import edu.greenchannel.common.ApiResponse;
import edu.greenchannel.dashboard.service.DashboardPushService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard/ws")
public class WebSocketController {
    private final DashboardPushService dashboardPushService;

    public WebSocketController(DashboardPushService dashboardPushService) {
        this.dashboardPushService = dashboardPushService;
    }

    @PostMapping("/push/all")
    @RequirePermission("school:dashboard:view")
    public ApiResponse<String> pushAll() {
        dashboardPushService.manualPushAll();
        return ApiResponse.success("全量大屏数据推送任务已触发");
    }

    @GetMapping("/heartbeat")
    public ApiResponse<String> heartbeat() {
        return ApiResponse.success("WebSocket service is running");
    }
}
