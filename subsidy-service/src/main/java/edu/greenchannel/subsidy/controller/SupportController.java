package edu.greenchannel.subsidy.controller;

import edu.greenchannel.auth.AuthInterceptor;
import edu.greenchannel.auth.CurrentUser;
import edu.greenchannel.auth.RequirePermission;
import edu.greenchannel.common.ApiResponse;
import edu.greenchannel.subsidy.service.SupportService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/subsidy")
public class SupportController {

    private final SupportService supportService;

    public SupportController(SupportService supportService) {
        this.supportService = supportService;
    }

    @GetMapping("/plans")
    @RequirePermission({"student:subsidy:view", "school:batch:view"})
    public ApiResponse<List<Map<String, Object>>> listPlans(
            @RequestParam(required = false) Integer status,
            @RequestAttribute(AuthInterceptor.CURRENT_USER_ATTRIBUTE) CurrentUser user) {
        return ApiResponse.success(supportService.listPlans(status, user));
    }

    @PostMapping("/plans")
    @RequirePermission("school:batch:view")
    public ApiResponse<Map<String, Object>> createPlan(
            @Valid @RequestBody AidPlanRequest request,
            @RequestAttribute(AuthInterceptor.CURRENT_USER_ATTRIBUTE) CurrentUser user) {
        return ApiResponse.success(Map.of("id", supportService.createPlan(request, user.id())));
    }

    @PutMapping("/plans/{id}")
    @RequirePermission("school:batch:view")
    public ApiResponse<Void> updatePlan(@PathVariable long id, @Valid @RequestBody AidPlanRequest request) {
        supportService.updatePlan(id, request);
        return ApiResponse.success();
    }

    @PostMapping("/plans/{id}/{action}")
    @RequirePermission("school:batch:view")
    public ApiResponse<Void> changePlanStatus(@PathVariable long id, @PathVariable String action) {
        supportService.changePlanStatus(id, action);
        return ApiResponse.success();
    }

    @PostMapping("/plans/{id}/estimate")
    @RequirePermission("school:batch:view")
    public ApiResponse<Map<String, Object>> estimatePlan(@PathVariable long id) {
        return ApiResponse.success(supportService.estimatePlan(id));
    }

    @PostMapping("/appeals")
    @RequirePermission("student:subsidy:view")
    public ApiResponse<Map<String, Object>> createAppeal(
            @Valid @RequestBody AppealCreateRequest request,
            @RequestAttribute(AuthInterceptor.CURRENT_USER_ATTRIBUTE) CurrentUser user) {
        return ApiResponse.success(Map.of("id", supportService.createAppeal(request, user)));
    }

    @GetMapping("/appeals/my")
    @RequirePermission("student:subsidy:view")
    public ApiResponse<List<Map<String, Object>>> myAppeals(
            @RequestAttribute(AuthInterceptor.CURRENT_USER_ATTRIBUTE) CurrentUser user) {
        return ApiResponse.success(supportService.myAppeals(user.id()));
    }

    @PutMapping("/appeals/{id}")
    @RequirePermission("student:subsidy:view")
    public ApiResponse<Void> resubmitAppeal(
            @PathVariable long id,
            @Valid @RequestBody AppealSupplementRequest request,
            @RequestAttribute(AuthInterceptor.CURRENT_USER_ATTRIBUTE) CurrentUser user) {
        supportService.resubmitAppeal(id, request, user);
        return ApiResponse.success();
    }

    @GetMapping("/appeals/pending")
    @RequirePermission({"tutor:review:view", "college:review:view", "school:review:view"})
    public ApiResponse<List<Map<String, Object>>> pendingAppeals(
            @RequestAttribute(AuthInterceptor.CURRENT_USER_ATTRIBUTE) CurrentUser user) {
        return ApiResponse.success(supportService.pendingAppeals(user));
    }

    @PostMapping("/appeals/{id}/handle")
    @RequirePermission({"tutor:review:view", "college:review:view", "school:review:view"})
    public ApiResponse<Void> handleAppeal(
            @PathVariable long id,
            @Valid @RequestBody AppealHandleRequest request,
            @RequestAttribute(AuthInterceptor.CURRENT_USER_ATTRIBUTE) CurrentUser user) {
        supportService.handleAppeal(id, request, user);
        return ApiResponse.success();
    }

    @PostMapping("/surveys")
    @RequirePermission("school:fund:view")
    public ApiResponse<Map<String, Object>> createSurvey(
            @Valid @RequestBody SurveyCreateRequest request,
            @RequestAttribute(AuthInterceptor.CURRENT_USER_ATTRIBUTE) CurrentUser user) {
        return ApiResponse.success(Map.of("id", supportService.createSurvey(request, user.id())));
    }

    @PostMapping("/surveys/{id}/publish")
    @RequirePermission("school:fund:view")
    public ApiResponse<Void> publishSurvey(@PathVariable long id) {
        supportService.publishSurvey(id);
        return ApiResponse.success();
    }

    @GetMapping("/surveys")
    @RequirePermission({"student:subsidy:view", "school:fund:view"})
    public ApiResponse<List<Map<String, Object>>> listSurveys(
            @RequestAttribute(AuthInterceptor.CURRENT_USER_ATTRIBUTE) CurrentUser user) {
        return ApiResponse.success(supportService.listSurveys(user));
    }

    @PostMapping("/surveys/{id}/responses")
    @RequirePermission("student:subsidy:view")
    public ApiResponse<Void> submitSurvey(
            @PathVariable long id,
            @Valid @RequestBody SurveyResponseRequest request,
            @RequestAttribute(AuthInterceptor.CURRENT_USER_ATTRIBUTE) CurrentUser user) {
        supportService.submitSurvey(id, request, user.id());
        return ApiResponse.success();
    }

    @GetMapping("/surveys/{id}/summary")
    @RequirePermission("school:fund:view")
    public ApiResponse<Map<String, Object>> surveySummary(@PathVariable long id) {
        return ApiResponse.success(supportService.surveySummary(id));
    }

    public record AidPlanRequest(
            @NotBlank String planName,
            @NotBlank String fundSource,
            @NotBlank String amountMode,
            BigDecimal fixedAmount,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            @NotNull @Min(1) Integer quotaLimit,
            @NotNull LocalDate validStart,
            @NotNull LocalDate validEnd,
            String conditionExpression) {
    }

    public record AppealCreateRequest(
            @NotBlank String sourceType,
            @NotNull Long sourceApplyId,
            @NotBlank String reason,
            List<Long> attachmentIds) {
    }

    public record AppealHandleRequest(@NotBlank String action, @NotBlank String conclusion) {
    }

    public record AppealSupplementRequest(@NotBlank String reason, List<Long> attachmentIds) {
    }

    public record SurveyCreateRequest(
            @NotBlank String title,
            @NotBlank String targetType,
            Long targetBatchId,
            @NotNull LocalDate startDate,
            @NotNull LocalDate endDate) {
    }

    public record SurveyResponseRequest(
            @NotNull @Min(1) @Max(5) Integer score,
            String suggestion) {
    }
}
