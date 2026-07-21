package edu.dashboard.service;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ModuleRegistry {

    private final Map<String, ModuleDataService> moduleServices = new HashMap<>();
    private final List<ModuleDataService> services;

    public ModuleRegistry(List<ModuleDataService> services) {
        this.services = services;
    }

    /**
     * Spring Boot 3.x 正确写法
     * 初始化模块注册表
     */
    @PostConstruct
    public void init() {
        for (ModuleDataService service : services) {
            moduleServices.put(service.getModuleCode(), service);
            System.out.println("[ModuleRegistry] Registered module: " + service.getModuleCode());
        }
        System.out.println("[ModuleRegistry] Total modules registered: " + moduleServices.size());
    }

    public ModuleDataService getModuleService(String moduleCode) {
        ModuleDataService service = moduleServices.get(moduleCode);
        if (service == null) {
            throw new IllegalArgumentException(
                    "Module not found: " + moduleCode +
                            ", Available modules: " + moduleServices.keySet()
            );
        }
        return service;
    }

    public List<String> getAllModuleCodes() {
        return moduleServices.keySet().stream().toList();
    }
}