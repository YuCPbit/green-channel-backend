package edu.greenchannel.tutor.controller;

import edu.greenchannel.auth.AuthInterceptor;
import edu.greenchannel.auth.CurrentUser;
import edu.greenchannel.auth.RequirePermission;
import edu.greenchannel.common.ApiResponse;
import edu.greenchannel.common.PageResult;
import edu.greenchannel.tutor.dto.request.TutorApplyRequest;
import edu.greenchannel.tutor.dto.request.TutorReviewRequest;
import edu.greenchannel.tutor.dto.response.ApplyTypeResponse;
import edu.greenchannel.tutor.dto.response.StudentBrief;
import edu.greenchannel.tutor.dto.response.TutorApplyView;
import edu.greenchannel.tutor.service.TutorApplicationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 辅导员事务申请控制器
 */
@RestController
@RequestMapping("/api/tutor")
public class TutorApplicationController {

    private final TutorApplicationService service;

    public TutorApplicationController(TutorApplicationService service) {
        this.service = service;
    }

    private CurrentUser currentUser(HttpServletRequest request) {
        return (CurrentUser) request.getAttribute(AuthInterceptor.CURRENT_USER_ATTRIBUTE);
    }

    // ---- 申请类型 ----

    /**
     * 获取所有启用的申请类型
     */
    @GetMapping("/apply-types")
    public ApiResponse<List<ApplyTypeResponse>> getApplyTypes() {
        return ApiResponse.success(service.getApplyTypes());
    }

    // ---- 辅导员申请 ----

    /**
     * 辅导员发起申请
     */
    @PostMapping("/applications")
    @RequirePermission("tutor:application:view")
    public ApiResponse<TutorApplyView> createApplication(
            @RequestBody TutorApplyRequest request, HttpServletRequest httpRequest) {
        return ApiResponse.success(service.createApplication(currentUser(httpRequest), request));
    }

    /**
     * 辅导员更新申请
     */
    @PutMapping("/applications/{id}")
    @RequirePermission("tutor:application:view")
    public ApiResponse<TutorApplyView> updateApplication(
            @PathVariable Long id, @RequestBody TutorApplyRequest request, HttpServletRequest httpRequest) {
        return ApiResponse.success(service.updateApplication(currentUser(httpRequest), id, request));
    }

    /**
     * 辅导员提交草稿
     */
    @PostMapping("/applications/{id}/submit")
    @RequirePermission("tutor:application:view")
    public ApiResponse<TutorApplyView> submitDraft(
            @PathVariable Long id, HttpServletRequest httpRequest) {
        return ApiResponse.success(service.submitDraft(currentUser(httpRequest), id));
    }

    /**
     * 辅导员查看自己的申请列表
     */
    @GetMapping("/applications/my")
    @RequirePermission("tutor:application:view")
    public ApiResponse<PageResult<TutorApplyView>> listMyApplications(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long typeId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest httpRequest) {
        return ApiResponse.success(service.listMyApplications(currentUser(httpRequest), status, typeId, page, size));
    }

    /**
     * 管理员查看待审批列表
     */
    @GetMapping("/applications/review")
    @RequirePermission("college:tutor-review:view")
    public ApiResponse<PageResult<TutorApplyView>> listPendingReviews(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long typeId,
            @RequestParam(required = false) Integer urgency,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest httpRequest) {
        return ApiResponse.success(service.listPendingReviews(currentUser(httpRequest), status, typeId, urgency, page, size));
    }

    /**
     * 获取申请详情
     */
    @GetMapping("/applications/{id}")
    public ApiResponse<TutorApplyView> getDetail(
            @PathVariable Long id, HttpServletRequest httpRequest) {
        return ApiResponse.success(service.getDetail(currentUser(httpRequest), id));
    }

    // ---- 审核 ----

    /**
     * 提交审核
     */
    @PostMapping("/reviews")
    public ApiResponse<Void> submitReview(
            @RequestBody TutorReviewRequest request, HttpServletRequest httpRequest) {
        service.submitReview(currentUser(httpRequest), request);
        return ApiResponse.success();
    }

    // ---- 学生搜索 ----

    /**
     * 搜索所管学生（辅导员选择关联学生用）
     */
    @GetMapping("/students/search")
    @RequirePermission("tutor:application:view")
    public ApiResponse<List<StudentBrief>> searchStudents(
            @RequestParam String keyword, HttpServletRequest httpRequest) {
        return ApiResponse.success(service.searchStudents(currentUser(httpRequest), keyword));
    }

    // ---- 资金下发 ----

    /**
     * 单笔资金下发
     */
    @PostMapping("/applications/{id}/disburse")
    @RequirePermission("school:tutor-disburse:view")
    public ApiResponse<Void> disburse(
            @PathVariable Long id, HttpServletRequest httpRequest) {
        service.disburse(currentUser(httpRequest), id);
        return ApiResponse.success();
    }

    /**
     * 批量资金下发
     */
    @PostMapping("/applications/batch-disburse")
    @RequirePermission("school:tutor-disburse:view")
    public ApiResponse<Map<String, Object>> batchDisburse(
            @RequestBody Map<String, List<Long>> body, HttpServletRequest httpRequest) {
        int count = service.batchDisburse(currentUser(httpRequest), body.get("ids"));
        return ApiResponse.success(Map.of("count", count));
    }

    /**
     * 查询资金下发列表（支持按状态筛选，0-不涉及 1-待下发 2-已下发）
     */
    @GetMapping("/disburse/list")
    public ApiResponse<PageResult<TutorApplyView>> listDisburse(
            @RequestParam(required = false) Integer disburseStatus,
            @RequestParam(required = false) Long typeId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest httpRequest) {
        return ApiResponse.success(service.listDisburse(currentUser(httpRequest), disburseStatus, typeId, page, size));
    }

    /**
     * 资金下发汇总统计
     */
    @GetMapping("/disburse/summary")
    public ApiResponse<Map<String, Object>> getDisburseSummary(HttpServletRequest httpRequest) {
        return ApiResponse.success(service.getDisburseSummary(currentUser(httpRequest)));
    }

    // ---- 统计 ----

    /**
     * 获取辅导员事务申请统计
     */
    @GetMapping("/statistics")
    public ApiResponse<Map<String, Object>> getStatistics(HttpServletRequest httpRequest) {
        return ApiResponse.success(service.getStatistics(currentUser(httpRequest)));
    }
}
