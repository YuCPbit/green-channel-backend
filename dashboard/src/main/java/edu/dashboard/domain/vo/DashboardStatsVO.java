package edu.dashboard.domain.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DashboardStatsVO {
    private Long totalApplicants;          // 累计申请总人数 (大礼包+补助)
    private Long todayNewApplicants;       // 今日新增人数
    private Double approvalRate;           // 审批通过率 (百分比，如 98.5)
    private BigDecimal totalSubsidyAmount; // 补助发放总额
    private Long giftPackCount;            // 大礼包发放总数
    private LocalDateTime updateTime;      // 数据更新时间
    // ===== 勤工助学统一口径指标 =====
    /**
     * 总岗位数：gc_work_study_position.status=2（已上架）的岗位总数
     */
    private Long wsTotalPositions;
    /**
     * 总报名人次：gc_work_study_apply.status=1（已报名）的总次数
     */
    private Long wsTotalApplications;
    /**
     * 总录用人数：gc_work_study_hire.hire_status=1（在岗）的总人数
     */
    private Long wsTotalHired;
    /**
     * 本月出勤率：(本月实际总出勤时长 / 本月应出勤总时长) × 100%
     * 应出勤总时长 = 在岗人数 × 每周8小时 × 本月周数 × 4（按每月4周估算）
     */
    private Double wsAttendanceRate;
    /**
     * 本月累计薪酬：gc_work_study_salary.status=3（已审批）的final_amount总和
     */
    private BigDecimal wsMonthlySalary;
}