package edu.greenchannel.workstudy.controller;

import edu.greenchannel.auth.AuthInterceptor;
import edu.greenchannel.auth.CurrentUser;
import edu.greenchannel.auth.RequirePermission;
import edu.greenchannel.common.ApiResponse;
import edu.greenchannel.workstudy.entity.WorkStudyAgreement;
import edu.greenchannel.workstudy.service.WorkStudyAgreementService;
import edu.greenchannel.workstudy.service.WorkStudyIdentityService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/workstudy/agreement")
@RequiredArgsConstructor
public class WorkStudyAgreementController {

    private final WorkStudyAgreementService agreementService;
    private final WorkStudyIdentityService identityService;

    /**
     * 学生签署协议
     */
    @PostMapping("/sign")
    @RequirePermission("workstudy:agreement:sign")
    public ApiResponse<String> sign(@RequestParam Long agreementId,
                                    @RequestAttribute(AuthInterceptor.CURRENT_USER_ATTRIBUTE) CurrentUser currentUser) {
        agreementService.signAgreement(
                agreementId, identityService.requireStudentId(currentUser.id()));
        return ApiResponse.success("签署成功");
    }

    /**
     * 协议续签（用工部门）
     */
    @PostMapping("/{agreementId}/renew")
    @RequirePermission("workstudy:agreement:renew")
    public ApiResponse<String> renew(@PathVariable Long agreementId,
                                     @RequestParam(required = false) LocalDate newEndDate,
                                     @RequestAttribute(AuthInterceptor.CURRENT_USER_ATTRIBUTE) CurrentUser currentUser) {
        agreementService.renewAgreement(agreementId, currentUser.id(), newEndDate);
        return ApiResponse.success("续签成功");
    }

    /**
     * 查询协议详情
     */
    @GetMapping("/{agreementId}")
    @RequirePermission({"workstudy:agreement:view", "workstudy:agreement:renew"})
    public ApiResponse<WorkStudyAgreement> getAgreement(
            @PathVariable Long agreementId,
            @RequestAttribute(AuthInterceptor.CURRENT_USER_ATTRIBUTE) CurrentUser currentUser) {
        long subjectId = currentUser.userType() == 1
                ? identityService.requireStudentId(currentUser.id())
                : currentUser.id();
        return ApiResponse.success(agreementService.getAccessibleAgreement(
                agreementId, subjectId, currentUser.userType()));
    }

    /**
     * 查询学生的协议列表
     */
    @GetMapping("/student")
    @RequirePermission("workstudy:agreement:view")
    public ApiResponse<?> getStudentAgreements(@RequestAttribute(AuthInterceptor.CURRENT_USER_ATTRIBUTE) CurrentUser currentUser) {
        return ApiResponse.success(agreementService.listAgreements(
                identityService.requireStudentId(currentUser.id()), null));
    }

    /**
     * 管理端查询协议列表，供到期检查与续签使用。
     */
    @GetMapping("/list")
    @RequirePermission("workstudy:agreement:renew")
    public ApiResponse<?> listAgreements(
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Integer signStatus) {
        return ApiResponse.success(agreementService.listAgreements(studentId, signStatus));
    }
}
