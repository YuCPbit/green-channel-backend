package edu.greenchannel.subsidy.controller;

import edu.greenchannel.auth.AuthInterceptor;
import edu.greenchannel.auth.CurrentUser;
import edu.greenchannel.auth.RequirePermission;
import edu.greenchannel.common.ApiResponse;
import edu.greenchannel.common.PageResult;
import edu.greenchannel.subsidy.dto.request.SubsidyApplyRequest;
import edu.greenchannel.subsidy.service.SubsidyApplyService;
import edu.greenchannel.subsidy.dto.response.SubsidyApplyView;
import edu.greenchannel.subsidy.dto.request.SubsidyReviewRequest;
import edu.greenchannel.subsidy.repository.SubsidyApplyRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/subsidy")
public class SubsidyApplyController {

    private final SubsidyApplyService service;

    public SubsidyApplyController(SubsidyApplyService service) {
        this.service = service;
    }

    private CurrentUser currentUser(HttpServletRequest request) {
        return (CurrentUser) request.getAttribute(AuthInterceptor.CURRENT_USER_ATTRIBUTE);
    }

    // ---- Applies ----

    @PostMapping("/applies")
    @RequirePermission("student:subsidy:view")
    public ApiResponse<SubsidyApplyView> submitApply(
            @RequestBody SubsidyApplyRequest request, HttpServletRequest httpRequest) {
        return ApiResponse.success(service.submitStudentApply(currentUser(httpRequest), request));
    }

    @PostMapping("/applies/tutor")
    @RequirePermission("tutor:review:view")
    public ApiResponse<SubsidyApplyView> submitTutorApply(
            @RequestBody SubsidyApplyRequest request, HttpServletRequest httpRequest) {
        return ApiResponse.success(service.submitTutorApply(currentUser(httpRequest), request));
    }

    @GetMapping("/applies")
    public ApiResponse<PageResult<SubsidyApplyView>> listApplies(
            @RequestParam(required = false) Long batchId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String studentName,
            @RequestParam(name = "collegeId", required = false) Long collegeId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest httpRequest) {
        return ApiResponse.success(service.listApplies(currentUser(httpRequest), batchId, status, studentName, page, size));
    }

    @GetMapping("/applies/{id}")
    public ApiResponse<SubsidyApplyView> getDetail(@PathVariable long id, HttpServletRequest httpRequest) {
        return ApiResponse.success(service.getApplyDetail(currentUser(httpRequest), id));
    }

    @PutMapping("/applies/{id}")
    @RequirePermission("student:subsidy:view")
    public ApiResponse<SubsidyApplyView> resubmitApply(
            @PathVariable long id, @RequestBody SubsidyApplyRequest request, HttpServletRequest httpRequest) {
        return ApiResponse.success(service.resubmitApply(currentUser(httpRequest), id, request));
    }

    // ---- Reviews ----

    @PostMapping("/reviews")
    public ApiResponse<Void> submitReview(@RequestBody SubsidyReviewRequest request, HttpServletRequest httpRequest) {
        service.submitReview(currentUser(httpRequest), request);
        return ApiResponse.success(null);
    }

    // ---- Supporting endpoints ----

    @GetMapping("/batches/available")
    public ApiResponse<List<SubsidyApplyRepository.BatchInfo>> availableBatches(
            @RequestParam(required = false) Integer userType, HttpServletRequest httpRequest) {
        return ApiResponse.success(service.getAvailableBatches());
    }

    @GetMapping("/students/search")
    public ApiResponse<List<SubsidyApplyRepository.StudentBrief>> searchStudents(
            @RequestParam String keyword, HttpServletRequest httpRequest) {
        return ApiResponse.success(service.searchStudents(currentUser(httpRequest), keyword));
    }
}
