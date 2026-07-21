package edu.dashboard.controller;

import edu.dashboard.common.Result;
import edu.dashboard.service.ModuleRegistry;
import edu.dashboard.service.ModuleDataService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final ModuleRegistry moduleRegistry;

    public DashboardController(ModuleRegistry moduleRegistry) {
        this.moduleRegistry = moduleRegistry;
    }

    /**
     * 统一核心指标接口
     * GET /api/dashboard/stats?module=base
     * GET /api/dashboard/stats?module=workstudy
     */
    @GetMapping("/stats")
    public Result<Object> getStats(@RequestParam String module) {
        ModuleDataService service = moduleRegistry.getModuleService(module);
        return Result.success(service.getCoreStats());
    }

    /**
     * 统一图表数据接口
     * GET /api/dashboard/chart?module=workstudy&type=position-stats
     */
    @GetMapping("/chart")
    public Result<Object> getChartData(@RequestParam String module, @RequestParam String type) {
        ModuleDataService service = moduleRegistry.getModuleService(module);
        return Result.success(service.getChartData(type));
    }
}