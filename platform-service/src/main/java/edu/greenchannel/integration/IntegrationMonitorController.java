package edu.greenchannel.integration;

import edu.greenchannel.auth.RequirePermission;
import edu.greenchannel.common.ApiResponse;
import edu.greenchannel.common.BusinessException;
import edu.greenchannel.common.PageResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/system/integrations/calls")
@RequirePermission("system:integration:view")
public class IntegrationMonitorController {
    private final IntegrationRepository repository;

    public IntegrationMonitorController(IntegrationRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public ApiResponse<PageResult<IntegrationCallLog>> search(
            @RequestParam(required = false) String clientId,
            @RequestParam(required = false) Boolean success,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        if (page < 1 || size < 1 || size > 100) {
            throw new BusinessException(40001, "分页参数不正确");
        }
        return ApiResponse.success(repository.search(clientId, success, page, size));
    }
}
