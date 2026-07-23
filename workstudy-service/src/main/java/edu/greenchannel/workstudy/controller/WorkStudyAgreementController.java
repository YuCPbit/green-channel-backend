package edu.greenchannel.workstudy.controller;

import edu.greenchannel.auth.AuthInterceptor;
import edu.greenchannel.auth.CurrentUser;
import edu.greenchannel.auth.RequirePermission;
import edu.greenchannel.common.ApiResponse;
import edu.greenchannel.common.BusinessException;
import edu.greenchannel.workstudy.entity.WorkStudyAgreement;
import edu.greenchannel.workstudy.service.WorkStudyAgreementService;
import edu.greenchannel.workstudy.service.impl.WorkStudyAgreementServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/workstudy/agreement")
@RequiredArgsConstructor
public class WorkStudyAgreementController {

    private final WorkStudyAgreementService agreementService;

    /**
     * 学生签署协议
     */
    @PostMapping("/sign")
    @RequirePermission("workstudy:agreement:sign")
    public ApiResponse<String> sign(@RequestParam Long agreementId,
                                    @RequestAttribute(AuthInterceptor.CURRENT_USER_ATTRIBUTE) CurrentUser currentUser) {
        agreementService.signAgreement(agreementId, currentUser.id());
        return ApiResponse.success("签署成功");
    }

    /**
     * 协议续签（用工部门）
     */
    @PostMapping("/{agreementId}/renew")
    @RequirePermission("workstudy:agreement:renew")
    public ApiResponse<String> renew(@PathVariable Long agreementId,
                                     @RequestAttribute(AuthInterceptor.CURRENT_USER_ATTRIBUTE) CurrentUser currentUser) {
        ((WorkStudyAgreementServiceImpl) agreementService).renewAgreement(agreementId, currentUser.id());
        return ApiResponse.success("续签成功");
    }

    /**
     * 查询协议详情
     */
    @GetMapping("/{agreementId}")
    @RequirePermission("workstudy:agreement:view")
    public ApiResponse<WorkStudyAgreement> getAgreement(@PathVariable Long agreementId) {
        WorkStudyAgreement agreement = agreementService.getById(agreementId);
        if (agreement == null || agreement.getDeleted() == 1) {
            throw new BusinessException(40400, "协议不存在");
        }
        return ApiResponse.success(agreement);
    }

    /**
     * 查询学生的协议列表
     */
    @GetMapping("/student")
    @RequirePermission("workstudy:agreement:view")
    public ApiResponse<?> getStudentAgreements(@RequestAttribute(AuthInterceptor.CURRENT_USER_ATTRIBUTE) CurrentUser currentUser) {
        Long studentId = currentUser.id();
        return ApiResponse.success(agreementService.lambdaQuery()
                .eq(WorkStudyAgreement::getStudentId, studentId)
                .eq(WorkStudyAgreement::getDeleted, 0)
                .orderByDesc(WorkStudyAgreement::getCreateTime)
                .list());
    }
}