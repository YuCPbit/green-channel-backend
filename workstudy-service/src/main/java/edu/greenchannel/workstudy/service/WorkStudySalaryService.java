package edu.greenchannel.workstudy.service;

import com.baomidou.mybatisplus.spring.service.IService;
import edu.greenchannel.workstudy.entity.WorkStudySalary;
import java.time.YearMonth;

public interface WorkStudySalaryService extends IService<WorkStudySalary> {

    /**
     * 核算指定年月的薪酬
     * @param yearMonth 年月（如 2026-07）
     * @return 生成的薪酬记录数
     */
    int calculateMonthlySalary(YearMonth yearMonth);
}