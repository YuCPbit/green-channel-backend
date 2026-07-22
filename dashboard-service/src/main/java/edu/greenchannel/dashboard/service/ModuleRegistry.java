package edu.greenchannel.dashboard.service;

import edu.greenchannel.common.BusinessException;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class ModuleRegistry {
    private final Map<String, ModuleDataService> services;

    public ModuleRegistry(List<ModuleDataService> moduleServices) {
        Map<String, ModuleDataService> registered = new LinkedHashMap<>();
        for (ModuleDataService service : moduleServices) {
            ModuleDataService duplicate = registered.put(service.getModuleCode(), service);
            if (duplicate != null) {
                throw new IllegalStateException("重复的大屏模块编码: " + service.getModuleCode());
            }
        }
        this.services = Map.copyOf(registered);
    }

    public ModuleDataService getModuleService(String moduleCode) {
        ModuleDataService service = services.get(moduleCode);
        if (service == null) {
            throw new BusinessException(40000, "不支持的大屏模块: " + moduleCode);
        }
        return service;
    }

    public List<String> getAllModuleCodes() {
        return services.keySet().stream().sorted().toList();
    }
}
