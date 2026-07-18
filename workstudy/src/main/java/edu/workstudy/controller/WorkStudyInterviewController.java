package edu.workstudy.controller;

import edu.workstudy.common.Result;
import edu.workstudy.service.WorkStudyApplyService;
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
    public Result<?> recordResult(
            @RequestParam Long applyId,
            @RequestParam Long interviewerId, // 模拟登录用户
            @RequestParam Integer interviewStatus,
            @RequestParam(required = false) String remark) {

        applyService.recordInterviewResult(applyId, interviewerId, interviewStatus, remark);
        return Result.success("面试结果录入成功");
    }
}