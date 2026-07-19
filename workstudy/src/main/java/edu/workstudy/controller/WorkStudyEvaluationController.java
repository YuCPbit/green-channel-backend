package edu.workstudy.controller;

import edu.workstudy.common.Result;
import edu.workstudy.entity.WorkStudyEvaluation;
import edu.workstudy.service.WorkStudyEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/evaluation")
@RequiredArgsConstructor
public class WorkStudyEvaluationController {
    private final WorkStudyEvaluationService evaluationService;

    /**
     * 提交评价
     */
    @PostMapping("/submit")
    public Result<String> submit(@RequestBody WorkStudyEvaluation evaluation) {
        evaluationService.submitEvaluation(evaluation);
        return Result.success("评价提交成功");
    }

    /**
     * 查询某学生某月评价
     */
    @GetMapping("/query")
    public Result<WorkStudyEvaluation> query(@RequestParam Long studentId, @RequestParam int year, @RequestParam int month) {
        WorkStudyEvaluation eval = evaluationService.lambdaQuery()
                .eq(WorkStudyEvaluation::getStudentId, studentId)
                .eq(WorkStudyEvaluation::getEvalYear, year)
                .eq(WorkStudyEvaluation::getEvalMonth, month)
                .one();
        return Result.success(eval);
    }
}