package edu.greenchannel.workstudy.controller;

import edu.greenchannel.auth.AuthInterceptor;
import edu.greenchannel.auth.CurrentUser;
import edu.greenchannel.auth.RequirePermission;
import edu.greenchannel.common.ApiResponse;
import edu.greenchannel.common.BusinessException;
import edu.greenchannel.workstudy.entity.WorkStudyPosition;
import edu.greenchannel.workstudy.service.WorkStudyPositionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workstudy/position")
@RequiredArgsConstructor
public class WorkStudyPositionController {

    private final WorkStudyPositionService positionService;

    /**
     * 发布岗位（用工部门）
     */
    @PostMapping
    @RequirePermission("workstudy:position:publish")
    public ApiResponse<Long> publishPosition(@RequestBody WorkStudyPosition position,
                                             @RequestAttribute(AuthInterceptor.CURRENT_USER_ATTRIBUTE) CurrentUser currentUser) {
        Long positionId = positionService.publishPosition(position, currentUser.id());
        return ApiResponse.success(positionId);
    }

    /**
     * 提交审核
     */
    @PutMapping("/{positionId}/submit")
    @RequirePermission("workstudy:position:submit")
    public ApiResponse<Void> submitForApproval(@PathVariable Long positionId,
                                               @RequestAttribute(AuthInterceptor.CURRENT_USER_ATTRIBUTE) CurrentUser currentUser) {
        positionService.submitForApproval(positionId, currentUser.id());
        return ApiResponse.success();
    }

    /**
     * 审核岗位（学校资助中心）
     */
    @PutMapping("/{positionId}/approve")
    @RequirePermission("workstudy:position:approve")
    public ApiResponse<Void> approvePosition(@PathVariable Long positionId,
                                             @RequestParam boolean approved,
                                             @RequestParam(required = false) String rejectReason,
                                             @RequestAttribute(AuthInterceptor.CURRENT_USER_ATTRIBUTE) CurrentUser currentUser) {
        positionService.approvePosition(positionId, approved, rejectReason, currentUser.id());
        return ApiResponse.success();
    }

    /**
     * 获取岗位列表
     */
    @GetMapping("/list")
    @RequirePermission("workstudy:position:view")
    public ApiResponse<List<WorkStudyPosition>> listPositions(
            @RequestParam(required = false) Long batchId,
            @RequestParam(required = false) Integer status,
            @RequestAttribute(AuthInterceptor.CURRENT_USER_ATTRIBUTE) CurrentUser currentUser) {
        if (currentUser.userType() == 1) {
            status = 2;
        }
        List<WorkStudyPosition> positions = positionService.listValidPositions(batchId, status);
        return ApiResponse.success(positions);
    }

    /**
     * 下架岗位
     */
    @PutMapping("/{positionId}/offline")
    @RequirePermission("workstudy:position:offline")
    public ApiResponse<Void> offlinePosition(@PathVariable Long positionId,
                                             @RequestAttribute(AuthInterceptor.CURRENT_USER_ATTRIBUTE) CurrentUser currentUser) {
        positionService.offlinePosition(positionId, currentUser.id());
        return ApiResponse.success();
    }

    /**
     * 更新岗位信息
     */
    @PutMapping("/{positionId}")
    @RequirePermission("workstudy:position:update")
    public ApiResponse<Void> updatePosition(@PathVariable Long positionId,
                                            @RequestBody WorkStudyPosition position,
                                            @RequestAttribute(AuthInterceptor.CURRENT_USER_ATTRIBUTE) CurrentUser currentUser) {
        position.setId(positionId);
        positionService.updatePosition(position, currentUser.id());
        return ApiResponse.success();
    }

    /**
     * 获取岗位详情
     */
    @GetMapping("/{positionId}")
    @RequirePermission("workstudy:position:view")
    public ApiResponse<WorkStudyPosition> getPositionDetail(
            @PathVariable Long positionId,
            @RequestAttribute(AuthInterceptor.CURRENT_USER_ATTRIBUTE) CurrentUser currentUser) {
        WorkStudyPosition position = positionService.getById(positionId);
        if (position == null || position.getDeleted() == 1) {
            throw new BusinessException(40400, "岗位不存在");
        }
        if (currentUser.userType() == 1 && position.getStatus() != 2) {
            throw new BusinessException(40400, "岗位不存在或未开放");
        }
        return ApiResponse.success(position);
    }
}
