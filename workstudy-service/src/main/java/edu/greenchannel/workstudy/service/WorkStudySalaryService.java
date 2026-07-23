package edu.greenchannel.workstudy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.greenchannel.workstudy.entity.WorkStudySalary;
import java.math.BigDecimal;
import java.time.YearMonth;

public interface WorkStudySalaryService extends IService<WorkStudySalary> {

    /**
     * 核算指定年月的薪酬
     */
    int calculateMonthlySalary(YearMonth yearMonth);

    /**
     * 部门确认薪酬
     */
    void confirmByDepartment(Long salaryId, Long deptUserId, BigDecimal confirmedAmount);

    /**
     * 资助中心审批薪酬
     */
    void approveBySchool(Long salaryId, Long schoolUserId, BigDecimal finalAmount);

    /**
     * 标记薪酬已发放
     */
    void markAsPaid(Long salaryId, Long operatorId);
}