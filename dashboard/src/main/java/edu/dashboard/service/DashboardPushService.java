package edu.dashboard.service;

import edu.dashboard.domain.vo.DashboardStatsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@EnableScheduling
public class DashboardPushService {

    @Autowired
    private ModuleRegistry moduleRegistry;

    @Autowired
    private WebSocketService webSocketService;

    /**
     * 免登录大屏数据定时推送
     * 每分钟推送一次所有模块的统计数据
     */
    @Scheduled(fixedDelay = 60000)
    public void pushAllModuleStats() {
        System.out.println("[" + LocalDateTime.now() + "] Starting dashboard data push...");

        List<String> moduleCodes = moduleRegistry.getAllModuleCodes();

        for (String moduleCode : moduleCodes) {
            try {
                ModuleDataService moduleService = moduleRegistry.getModuleService(moduleCode);
                Object coreStats = moduleService.getCoreStats();

                if (coreStats != null) {
                    webSocketService.broadcastModuleStats(moduleCode, coreStats);
                    System.out.println("  ✓ Pushed stats for module: " + moduleCode);
                }
            } catch (Exception e) {
                System.err.println("  ✗ Failed to push stats for module: " + moduleCode + ", Error: " + e.getMessage());
            }
        }
        System.out.println("[" + LocalDateTime.now() + "] Dashboard data push completed");
    }

    /**
     * 手动触发全量推送
     */
    public void manualPushAll() {
        pushAllModuleStats();
    }
}