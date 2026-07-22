package edu.greenchannel.workstudy.controller;

import edu.greenchannel.common.ApiResponse;
import edu.greenchannel.auth.AuthInterceptor;
import edu.greenchannel.auth.CurrentUser;
import edu.greenchannel.auth.RequirePermission;
import edu.greenchannel.workstudy.service.WorkStudyApplyService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/interview")
public class WorkStudyInterviewController {

    private final WorkStudyApplyService applyService;

    public WorkStudyInterviewController(WorkStudyApplyService applyService) {
        this.applyService = applyService;
    }

    /**
     * 录入面试结果
     * 面试通过: interviewStatus=2
     * 面试不通过: interviewStatus=3
     */
    @PostMapping("/record-result")
    @RequirePermission("school:workstudy:edit")
    public ApiResponse<?> recordResult(
            @RequestParam Long applyId,
            @RequestAttribute(AuthInterceptor.CURRENT_USER_ATTRIBUTE) CurrentUser currentUser,
            @RequestParam Integer interviewStatus,
            @RequestParam(required = false) String remark) {

        applyService.recordInterviewResult(applyId, currentUser.id(), interviewStatus, remark);
        return ApiResponse.success("面试结果录入成功");
    }
}
