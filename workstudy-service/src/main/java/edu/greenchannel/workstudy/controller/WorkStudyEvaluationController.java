package edu.greenchannel.workstudy.controller;

import edu.greenchannel.common.ApiResponse;
import edu.greenchannel.auth.AuthInterceptor;
import edu.greenchannel.auth.CurrentUser;
import edu.greenchannel.auth.RequirePermission;
import edu.greenchannel.workstudy.entity.WorkStudyEvaluation;
import edu.greenchannel.workstudy.service.WorkStudyEvaluationService;
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
    @RequirePermission("school:workstudy:edit")
    public ApiResponse<String> submit(@RequestBody WorkStudyEvaluation evaluation,
            @RequestAttribute(AuthInterceptor.CURRENT_USER_ATTRIBUTE) CurrentUser currentUser) {
        evaluation.setEvaluatorId(currentUser.id());
        evaluationService.submitEvaluation(evaluation);
        return ApiResponse.success("评价提交成功");
    }

    /**
     * 查询某学生某月评价
     */
    @GetMapping("/query")
    @RequirePermission("school:workstudy:view")
    public ApiResponse<WorkStudyEvaluation> query(@RequestParam Long studentId, @RequestParam int year, @RequestParam int month) {
        WorkStudyEvaluation eval = evaluationService.lambdaQuery()
                .eq(WorkStudyEvaluation::getStudentId, studentId)
                .eq(WorkStudyEvaluation::getEvalYear, year)
                .eq(WorkStudyEvaluation::getEvalMonth, month)
                .one();
        return ApiResponse.success(eval);
    }
}
