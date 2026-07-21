package edu.dashboard.domain.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DashboardStatsVO {
    // ========== 基础模块指标 ==========
    private Long totalApplicants;          // 累计申请总人数
    private Long todayNewApplicants;       // 今日新增人数
    private Double approvalRate;           // 审批通过率
    private BigDecimal totalSubsidyAmount; // 补助发放总额
    private Long giftPackCount;            // 大礼包发放总数
    private LocalDateTime updateTime;      // 数据更新时间

    // ========== 勤工助学模块指标 ==========
    private Long wsTotalPositions;         // 总岗位数
    private Long wsTotalApplications;     // 总报名人次
    private Long wsTotalHired;            // 总录用人数
    private Double wsAttendanceRate;      // 本月出勤率
    private BigDecimal wsMonthlySalary;   // 本月累计薪酬
}