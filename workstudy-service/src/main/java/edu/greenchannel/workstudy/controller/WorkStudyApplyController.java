package edu.greenchannel.workstudy.controller;

import edu.greenchannel.auth.AuthInterceptor;
import edu.greenchannel.auth.CurrentUser;
import edu.greenchannel.auth.RequirePermission;
import edu.greenchannel.common.ApiResponse;
import edu.greenchannel.common.BusinessException;
import edu.greenchannel.workstudy.entity.WorkStudyApply;
import edu.greenchannel.workstudy.service.WorkStudyApplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workstudy/apply")
@RequiredArgsConstructor
public class WorkStudyApplyController {

    private final WorkStudyApplyService applyService;

    /**
     * 学生报名岗位
     */
    @PostMapping
    @RequirePermission("workstudy:apply:submit")
    public ApiResponse<Long> submitApply(@RequestBody WorkStudyApply applyInfo,
                                         @RequestAttribute(AuthInterceptor.CURRENT_USER_ATTRIBUTE) CurrentUser currentUser) {
        Long positionId = applyInfo.getPositionId();
        Long studentId = Long.parseLong(currentUser.username()); // 假设username是学号
        Long applyId = applyService.applyForPosition(positionId, studentId, applyInfo);
        return ApiResponse.success(applyId);
    }

    /**
     * 获取我的申请列表
     */
    @GetMapping("/my")
    @RequirePermission("workstudy:apply:view")
    public ApiResponse<List<WorkStudyApply>> myApplications(
            @RequestAttribute(AuthInterceptor.CURRENT_USER_ATTRIBUTE) CurrentUser currentUser) {
        Long studentId = Long.parseLong(currentUser.username());
        List<WorkStudyApply> applications = applyService.getStudentApplications(studentId);
        return ApiResponse.success(applications);
    }

    /**
     * 获取岗位的申请列表（用工部门）
     */
    @GetMapping("/position/{positionId}")
    @RequirePermission("workstudy:apply:review")
    public ApiResponse<List<WorkStudyApply>> getPositionApplications(
            @PathVariable Long positionId) {
        List<WorkStudyApply> applications = applyService.getPositionApplications(positionId);
        return ApiResponse.success(applications);
    }

    /**
     * 录入面试结果（用工部门）
     */
    @PutMapping("/{applyId}/interview")
    @RequirePermission("workstudy:apply:interview")
    public ApiResponse<Void> recordInterviewResult(@PathVariable Long applyId,
                                                   @RequestParam Integer interviewStatus) {
        applyService.recordInterviewResult(applyId, interviewStatus);
        return ApiResponse.success();
    }

    /**
     * 辅导员填写推荐意见
     */
    @PutMapping("/{applyId}/tutor-recommend")
    @RequirePermission("workstudy:apply:tutor-recommend")
    public ApiResponse<Void> addTutorRecommendation(@PathVariable Long applyId,
                                                    @RequestParam String recommendation) {
        applyService.addTutorRecommendation(applyId, recommendation);
        return ApiResponse.success();
    }

    /**
     * 获取申请详情
     */
    @GetMapping("/{applyId}")
    @RequirePermission("workstudy:apply:view")
    public ApiResponse<WorkStudyApply> getApplyDetail(@PathVariable Long applyId) {
        WorkStudyApply apply = applyService.getById(applyId);
        if (apply == null || apply.getDeleted() == 1) {
            throw new BusinessException(40400, "申请记录不存在");
        }
        return ApiResponse.success(apply);
    }
}