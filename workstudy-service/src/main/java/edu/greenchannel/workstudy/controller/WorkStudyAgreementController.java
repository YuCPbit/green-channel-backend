package edu.greenchannel.workstudy.controller;

import edu.greenchannel.common.ApiResponse;
import edu.greenchannel.auth.AuthInterceptor;
import edu.greenchannel.auth.CurrentUser;
import edu.greenchannel.auth.RequirePermission;
import edu.greenchannel.workstudy.service.WorkStudyAgreementService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/agreement")
@RequiredArgsConstructor
public class WorkStudyAgreementController {

    private final WorkStudyAgreementService agreementService;

    @PostMapping("/sign")
    @RequirePermission("student:workstudy:view")
    public ApiResponse<String> sign(@RequestParam Long agreementId,
                               @RequestAttribute(AuthInterceptor.CURRENT_USER_ATTRIBUTE) CurrentUser currentUser) {
        agreementService.signAgreement(agreementId, currentUser.id());
        return ApiResponse.success("签署成功");
    }
}
