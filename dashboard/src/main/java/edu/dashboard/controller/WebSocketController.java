package edu.dashboard.controller;

import edu.dashboard.common.Result;
import edu.dashboard.service.DashboardPushService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ws")
public class WebSocketController {

    private final DashboardPushService dashboardPushService;

    public WebSocketController(DashboardPushService dashboardPushService) {
        this.dashboardPushService = dashboardPushService;
    }

    /**
     * 手动触发全量大屏数据推送
     * POST /api/ws/push/all
     */
    @PostMapping("/push/all")
    public Result<String> pushAll() {
        dashboardPushService.manualPushAll();
        return Result.success("全量大屏数据推送任务已触发");
    }

    /**
     * 心跳检测接口
     * GET /api/ws/heartbeat
     */
    @RequestMapping("/heartbeat")
    public Result<String> heartbeat() {
        return Result.success("WebSocket service is running");
    }
}