package edu.greenchannel.workstudy.controller;

import edu.greenchannel.common.ApiResponse;
import edu.greenchannel.auth.AuthInterceptor;
import edu.greenchannel.auth.CurrentUser;
import edu.greenchannel.auth.RequirePermission;
import edu.greenchannel.workstudy.entity.WorkStudyApply;
import edu.greenchannel.workstudy.service.WorkStudyApplyService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/apply")
public class WorkStudyApplyController {

    private final WorkStudyApplyService applyService;

    public WorkStudyApplyController(WorkStudyApplyService applyService) {
        this.applyService = applyService;
    }

    @PostMapping("/submit")
    @RequirePermission("student:workstudy:view")
    public ApiResponse<Long> submitApply(
            @RequestParam Long positionId,
            @RequestAttribute(AuthInterceptor.CURRENT_USER_ATTRIBUTE) CurrentUser currentUser,
            @RequestBody WorkStudyApply applyInfo) {
        Long applyId = applyService.applyForPosition(positionId, currentUser.id(), applyInfo);
        return ApiResponse.success(applyId);
    }

    @GetMapping("/my-applications")
    @RequirePermission("student:workstudy:view")
    public ApiResponse<?> myApplications(
            @RequestAttribute(AuthInterceptor.CURRENT_USER_ATTRIBUTE) CurrentUser currentUser) {
        return ApiResponse.success(applyService.lambdaQuery()
                .eq(WorkStudyApply::getStudentId, currentUser.id())
                .list());
    }
}
