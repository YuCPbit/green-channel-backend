package edu.greenchannel.workstudy.controller;

import edu.greenchannel.common.ApiResponse;
import edu.greenchannel.auth.AuthInterceptor;
import edu.greenchannel.auth.CurrentUser;
import edu.greenchannel.auth.RequirePermission;
import edu.greenchannel.workstudy.entity.WorkStudyPosition;
import edu.greenchannel.workstudy.service.WorkStudyPositionService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/position")
public class WorkStudyPositionController {

    private final WorkStudyPositionService positionService;

    public WorkStudyPositionController(WorkStudyPositionService positionService) {
        this.positionService = positionService;
    }

    @PostMapping("/publish")
    @RequirePermission("school:workstudy:edit")
    public ApiResponse<Long> publishPosition(@RequestBody WorkStudyPosition position,
            @RequestAttribute(AuthInterceptor.CURRENT_USER_ATTRIBUTE) CurrentUser currentUser) {
        Long positionId = positionService.publishPosition(position, currentUser.id());
        return ApiResponse.success(positionId);
    }

    @GetMapping("/list")
    public ApiResponse<?> listPositions(@RequestParam(required = false) Long batchId) {
        // 简易查询，后续可改为分页
        return ApiResponse.success(positionService.lambdaQuery()
                .eq(batchId != null, WorkStudyPosition::getBatchId, batchId)
                .list());
    }
}
