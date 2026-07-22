package edu.greenchannel.workstudy.controller;

import edu.greenchannel.common.ApiResponse;
import edu.greenchannel.auth.AuthInterceptor;
import edu.greenchannel.auth.CurrentUser;
import edu.greenchannel.auth.RequirePermission;
import edu.greenchannel.workstudy.service.WorkStudyHireService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/hire")
public class WorkStudyHireController {

    private final WorkStudyHireService hireService;

    public WorkStudyHireController(WorkStudyHireService hireService) {
        this.hireService = hireService;
    }

    /**
     * 学校审批录用
     */
    @PostMapping("/approve")
    @RequirePermission("school:workstudy:edit")
    public ApiResponse<?> approveHire(@RequestParam Long applyId,
            @RequestAttribute(AuthInterceptor.CURRENT_USER_ATTRIBUTE) CurrentUser currentUser) {
        Long hireId = hireService.approveHire(applyId, currentUser.id());
        return ApiResponse.success(hireId);
    }
}
