package edu.dashboard.domain.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class WorkStudyStatsVO {
    private Long totalPositions;      // 总岗位数
    private Long totalApplications;   // 总报名人次
    private Long totalHired;          // 总录用人数
    private Double attendanceRate;    // 本月出勤率
    private BigDecimal monthlySalary; // 本月累计薪酬
    private Double budgetExecRate;    // 预算执行率
}