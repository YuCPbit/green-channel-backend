package edu.workstudy.controller;

import edu.workstudy.common.Result;
import edu.workstudy.entity.WorkStudyApply;
import edu.workstudy.service.WorkStudyApplyService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/workstudy/apply")
public class WorkStudyApplyController {

    private final WorkStudyApplyService applyService;

    public WorkStudyApplyController(WorkStudyApplyService applyService) {
        this.applyService = applyService;
    }

    @PostMapping("/submit")
    public Result<Long> submitApply(
            @RequestParam Long positionId,
            @RequestParam Long studentId,
            @RequestBody WorkStudyApply applyInfo) {
        Long applyId = applyService.applyForPosition(positionId, studentId, applyInfo);
        return Result.success(applyId);
    }

    @GetMapping("/my-applications")
    public Result<?> myApplications(@RequestParam Long studentId) {
        return Result.success(applyService.lambdaQuery()
                .eq(WorkStudyApply::getStudentId, studentId)
                .list());
    }
}