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
}