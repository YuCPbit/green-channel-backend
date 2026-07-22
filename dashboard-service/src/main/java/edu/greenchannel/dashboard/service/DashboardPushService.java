package edu.greenchannel.dashboard.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class DashboardPushService {
    private static final Logger log = LoggerFactory.getLogger(DashboardPushService.class);

    private final ModuleRegistry moduleRegistry;
    private final WebSocketService webSocketService;

    public DashboardPushService(ModuleRegistry moduleRegistry, WebSocketService webSocketService) {
        this.moduleRegistry = moduleRegistry;
        this.webSocketService = webSocketService;
    }

    @Scheduled(initialDelayString = "${dashboard.push.initial-delay-ms:60000}",
            fixedDelayString = "${dashboard.push.interval-ms:60000}")
    public void pushAllModuleStats() {
        for (String moduleCode : moduleRegistry.getAllModuleCodes()) {
            try {
                Object stats = moduleRegistry.getModuleService(moduleCode).getCoreStats();
                webSocketService.broadcastModuleStats(moduleCode, stats);
            } catch (RuntimeException exception) {
                log.warn("Dashboard push failed for module {}", moduleCode, exception);
            }
        }
    }

    public void manualPushAll() {
        pushAllModuleStats();
    }
}
