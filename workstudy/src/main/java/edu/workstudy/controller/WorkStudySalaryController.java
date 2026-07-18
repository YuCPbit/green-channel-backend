package edu.workstudy.controller;

import edu.workstudy.common.Result;
import edu.workstudy.service.WorkStudySalaryService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;

@RestController
@RequestMapping("/salary")
public class WorkStudySalaryController {

    private final WorkStudySalaryService salaryService;

    public WorkStudySalaryController(WorkStudySalaryService salaryService) {
        this.salaryService = salaryService;
    }

    /**
     * 核算指定年月的薪酬
     * 例如：/api/workstudy/salary/calculate?year=2026&month=7
     */
    @PostMapping("/calculate")
    public Result<?> calculateSalary(@RequestParam int year, @RequestParam int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        int count = salaryService.calculateMonthlySalary(yearMonth);
        return Result.success("薪酬核算完成，共生成 " + count + " 条记录");
    }
}