package edu.greenchannel.workstudy.controller;

import edu.greenchannel.common.ApiResponse;
import edu.greenchannel.auth.AuthInterceptor;
import edu.greenchannel.auth.CurrentUser;
import edu.greenchannel.auth.RequirePermission;
import edu.greenchannel.workstudy.dto.ActiveHireVO;
import edu.greenchannel.workstudy.dto.PageResult;
import edu.greenchannel.workstudy.dto.WorkStudyEvaluationVO;
import edu.greenchannel.workstudy.entity.WorkStudyEvaluation;
import edu.greenchannel.workstudy.service.WorkStudyEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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

    /**
     * 分页查询评价列表
     */
    @GetMapping("/list")
    @RequirePermission("school:workstudy:view")
    public ApiResponse<PageResult<WorkStudyEvaluationVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long hireId,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Integer evalYear,
            @RequestParam(required = false) Integer evalMonth) {
        PageResult<WorkStudyEvaluationVO> result = evaluationService.listEvaluations(page, size, hireId, studentId, evalYear, evalMonth);
        return ApiResponse.success(result);
    }

    /**
     * 查询评价详情（含学生姓名、岗位等信息）
     */
    @GetMapping("/detail")
    @RequirePermission("school:workstudy:view")
    public ApiResponse<WorkStudyEvaluationVO> detail(@RequestParam Long id) {
        return ApiResponse.success(evaluationService.getDetail(id));
    }

    /**
     * 更新评价（仅允许修改评分和评语）
     */
    @PostMapping("/update")
    @RequirePermission("school:workstudy:edit")
    public ApiResponse<String> update(@RequestBody Map<String, Object> body,
            @RequestAttribute(AuthInterceptor.CURRENT_USER_ATTRIBUTE) CurrentUser currentUser) {
        Long id = Long.valueOf(body.get("id").toString());
        Integer score = Integer.valueOf(body.get("score").toString());
        String comment = body.get("comment") != null ? body.get("comment").toString() : null;
        evaluationService.updateEvaluation(id, score, comment, currentUser.id());
        return ApiResponse.success("评价更新成功");
    }

    /**
     * 删除评价（软删除）
     */
    @PostMapping("/delete")
    @RequirePermission("school:workstudy:edit")
    public ApiResponse<String> delete(@RequestParam Long id) {
        evaluationService.deleteEvaluation(id);
        return ApiResponse.success("评价删除成功");
    }

    /**
     * 学生查看本人评价（仅返回当前登录学生本人的评价，忽略客户端传入的studentId）
     */
    @GetMapping("/my")
    @RequirePermission("school:workstudy:view")
    public ApiResponse<PageResult<WorkStudyEvaluationVO>> myEvaluations(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Integer evalYear,
            @RequestParam(required = false) Integer evalMonth,
            @RequestAttribute(AuthInterceptor.CURRENT_USER_ATTRIBUTE) CurrentUser currentUser) {
        PageResult<WorkStudyEvaluationVO> result = evaluationService.getMyEvaluations(
                page, size, evalYear, evalMonth, currentUser);
        return ApiResponse.success(result);
    }

    /**
     * 获取在岗录用列表（支持姓名/学号搜索）
     */
    @GetMapping("/hire/active")
    @RequirePermission("school:workstudy:view")
    public ApiResponse<List<ActiveHireVO>> activeHires(@RequestParam(required = false) String keyword) {
        return ApiResponse.success(evaluationService.getActiveHires(keyword));
    }
}
