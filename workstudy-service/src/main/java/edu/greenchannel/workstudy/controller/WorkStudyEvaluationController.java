package edu.greenchannel.workstudy.controller;

import edu.greenchannel.auth.AuthInterceptor;
import edu.greenchannel.auth.CurrentUser;
import edu.greenchannel.auth.RequirePermission;
import edu.greenchannel.common.ApiResponse;
import edu.greenchannel.common.BusinessException;
import edu.greenchannel.workstudy.entity.WorkStudyEvaluation;
import edu.greenchannel.workstudy.service.WorkStudyEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/workstudy/evaluation")
@RequiredArgsConstructor
public class WorkStudyEvaluationController {

    private final WorkStudyEvaluationService evaluationService;

    /**
     * 提交评价（用工部门）
     */
    @PostMapping
    @RequirePermission("workstudy:evaluation:submit")
    public ApiResponse<String> submit(@RequestBody WorkStudyEvaluation evaluation,
                                      @RequestAttribute(AuthInterceptor.CURRENT_USER_ATTRIBUTE) CurrentUser currentUser) {
        evaluation.setEvaluatorId(currentUser.id());
        evaluationService.submitEvaluation(evaluation);
        return ApiResponse.success("评价提交成功");
    }

    /**
     * 查询某学生某月评价
     */
    @GetMapping
    @RequirePermission("workstudy:evaluation:view")
    public ApiResponse<WorkStudyEvaluation> query(@RequestParam Long studentId,
                                                  @RequestParam int year,
                                                  @RequestParam int month) {
        WorkStudyEvaluation eval = evaluationService.lambdaQuery()
                .eq(WorkStudyEvaluation::getStudentId, studentId)
                .eq(WorkStudyEvaluation::getEvalYear, year)
                .eq(WorkStudyEvaluation::getEvalMonth, month)
                .eq(WorkStudyEvaluation::getDeleted, 0)
                .one();

        if (eval == null) {
            throw new BusinessException(40400, "评价记录不存在");
        }
        return ApiResponse.success(eval);
    }

    /**
     * 查询学生历史评价
     */
    @GetMapping("/history/{studentId}")
    @RequirePermission("workstudy:evaluation:view")
    public ApiResponse<?> getHistory(@PathVariable Long studentId) {
        return ApiResponse.success(evaluationService.lambdaQuery()
                .eq(WorkStudyEvaluation::getStudentId, studentId)
                .eq(WorkStudyEvaluation::getDeleted, 0)
                .orderByDesc(WorkStudyEvaluation::getEvalYear)
                .orderByDesc(WorkStudyEvaluation::getEvalMonth)
                .list());
    }
}