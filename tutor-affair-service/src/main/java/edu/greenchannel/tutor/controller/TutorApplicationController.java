package edu.greenchannel.tutor.controller;

import edu.greenchannel.auth.AuthInterceptor;
import edu.greenchannel.auth.CurrentUser;
import edu.greenchannel.auth.RequirePermission;
import edu.greenchannel.common.ApiResponse;
import edu.greenchannel.common.PageResult;
import edu.greenchannel.tutor.dto.request.TutorApplyRequest;
import edu.greenchannel.tutor.dto.request.TutorReviewRequest;
import edu.greenchannel.tutor.dto.response.ApplyTypeResponse;
import edu.greenchannel.tutor.dto.response.LedgerDetailRow;
import edu.greenchannel.tutor.dto.response.LedgerSummaryRow;
import edu.greenchannel.tutor.dto.response.StudentBrief;
import edu.greenchannel.tutor.dto.response.TutorApplyView;
import edu.greenchannel.tutor.service.TutorApplicationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
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

    // ---- 台账 ----

    /**
     * 台账汇总（按学院+类型分组）
     */
    @GetMapping("/ledger/summary")
    @RequirePermission("school:tutor-disburse:view")
    public ApiResponse<List<LedgerSummaryRow>> getLedgerSummary(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            HttpServletRequest httpRequest) {
        LocalDate start = startDate != null && !startDate.isBlank() ? LocalDate.parse(startDate) : null;
        LocalDate end = endDate != null && !endDate.isBlank() ? LocalDate.parse(endDate) : null;
        return ApiResponse.success(service.getLedgerSummary(currentUser(httpRequest), start, end));
    }

    /**
     * 台账明细（与汇总口径一致）
     */
    @GetMapping("/ledger/detail")
    @RequirePermission("school:tutor-disburse:view")
    public ApiResponse<PageResult<LedgerDetailRow>> getLedgerDetail(
            @RequestParam(required = false) Long collegeId,
            @RequestParam(required = false) Long typeId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest httpRequest) {
        LocalDate start = startDate != null && !startDate.isBlank() ? LocalDate.parse(startDate) : null;
        LocalDate end = endDate != null && !endDate.isBlank() ? LocalDate.parse(endDate) : null;
        return ApiResponse.success(service.getLedgerDetail(currentUser(httpRequest), collegeId, typeId, start, end, page, size));
    }

    /**
     * 导出台账 Excel（支持 collegeIds/typeIds 逗号分隔多选）
     */
    @GetMapping("/ledger/export")
    @RequirePermission("school:tutor-disburse:view")
    public void exportLedgerExcel(
            @RequestParam(required = false) Long collegeId,
            @RequestParam(required = false) Long typeId,
            @RequestParam(required = false) String collegeIds,
            @RequestParam(required = false) String typeIds,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            HttpServletRequest httpRequest,
            HttpServletResponse response) throws IOException {
        LocalDate start = startDate != null && !startDate.isBlank() ? LocalDate.parse(startDate) : null;
        LocalDate end = endDate != null && !endDate.isBlank() ? LocalDate.parse(endDate) : null;

        List<Long> cIds = parseCsvIds(collegeIds);
        List<Long> tIds = parseCsvIds(typeIds);

        byte[] excelBytes = service.exportLedgerExcel(currentUser(httpRequest), collegeId, typeId, cIds, tIds, start, end);

        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=" + java.net.URLEncoder.encode("辅导员事务台账.xlsx", "UTF-8"));
        response.setContentLength(excelBytes.length);
        response.getOutputStream().write(excelBytes);
    }

    private List<Long> parseCsvIds(String csv) {
        if (csv == null || csv.isBlank()) return List.of();
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::parseLong)
                .toList();
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
