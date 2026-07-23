package edu.greenchannel.workstudy.controller;

import edu.greenchannel.auth.AuthInterceptor;
import edu.greenchannel.auth.CurrentUser;
import edu.greenchannel.auth.RequirePermission;
import edu.greenchannel.common.ApiResponse;
import edu.greenchannel.common.BusinessException;
import edu.greenchannel.workstudy.entity.WorkStudySalary;
import edu.greenchannel.workstudy.service.WorkStudySalaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.YearMonth;

@RestController
@RequestMapping("/api/workstudy/salary")
@RequiredArgsConstructor
public class WorkStudySalaryController {

    private final WorkStudySalaryService salaryService;

    /**
     * 核算指定年月的薪酬（资助中心）
     */
    @PostMapping("/calculate")
    @RequirePermission("workstudy:salary:calculate")
    public ApiResponse<String> calculateSalary(@RequestParam int year,
                                               @RequestParam int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        int count = salaryService.calculateMonthlySalary(yearMonth);
        return ApiResponse.success(String.format("薪酬核算完成，共生成 %d 条记录", count));
    }

    /**
     * 部门确认薪酬（用工部门）
     */
    @PostMapping("/{salaryId}/dept-confirm")
    @RequirePermission("workstudy:salary:dept-confirm")
    public ApiResponse<Void> confirmByDepartment(@PathVariable Long salaryId,
                                                 @RequestParam BigDecimal confirmedAmount,
                                                 @RequestAttribute(AuthInterceptor.CURRENT_USER_ATTRIBUTE) CurrentUser currentUser) {
        salaryService.confirmByDepartment(salaryId, currentUser.id(), confirmedAmount);
        return ApiResponse.success();
    }

    /**
     * 资助中心审批薪酬（学校）
     */
    @PostMapping("/{salaryId}/school-approve")
    @RequirePermission("workstudy:salary:school-approve")
    public ApiResponse<Void> approveBySchool(@PathVariable Long salaryId,
                                             @RequestParam BigDecimal finalAmount,
                                             @RequestAttribute(AuthInterceptor.CURRENT_USER_ATTRIBUTE) CurrentUser currentUser) {
        salaryService.approveBySchool(salaryId, currentUser.id(), finalAmount);
        return ApiResponse.success();
    }

    /**
     * 标记薪酬已发放（财务）
     */
    @PostMapping("/{salaryId}/mark-paid")
    @RequirePermission("workstudy:salary:mark-paid")
    public ApiResponse<Void> markAsPaid(@PathVariable Long salaryId,
                                        @RequestAttribute(AuthInterceptor.CURRENT_USER_ATTRIBUTE) CurrentUser currentUser) {
        salaryService.markAsPaid(salaryId, currentUser.id());
        return ApiResponse.success();
    }

    /**
     * 查询薪酬明细
     */
    @GetMapping("/{salaryId}")
    @RequirePermission("workstudy:salary:view")
    public ApiResponse<WorkStudySalary> getSalaryDetail(@PathVariable Long salaryId) {
        WorkStudySalary salary = salaryService.getById(salaryId);
        if (salary == null || salary.getDeleted() == 1) {
            throw new BusinessException(40400, "薪酬记录不存在");
        }
        return ApiResponse.success(salary);
    }

    /**
     * 查询学生薪酬列表
     */
    @GetMapping("/student")
    @RequirePermission("workstudy:salary:view")
    public ApiResponse<?> getStudentSalaries(@RequestAttribute(AuthInterceptor.CURRENT_USER_ATTRIBUTE) CurrentUser currentUser) {
        // 学生只能看自己的薪酬
        Long studentId = currentUser.id();
        return ApiResponse.success(salaryService.lambdaQuery()
                .eq(WorkStudySalary::getStudentId, studentId)
                .eq(WorkStudySalary::getDeleted, 0)
                .orderByDesc(WorkStudySalary::getSalaryYear)
                .orderByDesc(WorkStudySalary::getSalaryMonth)
                .list());
    }
}