package edu.greenchannel.workstudy.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("gc_work_study_salary")
public class WorkStudySalary {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 录用记录ID */
    private Long hireId;

    /** 学生ID */
    private Long studentId;

    /** 岗位ID */
    private Long positionId;

    /** 薪酬年份 */
    private Integer salaryYear;

    /** 薪酬月份 */
    private Integer salaryMonth;

    /**
     * 当月总工时(小时)
     * DECIMAL(6,2)
     */
    private BigDecimal totalWorkHours;

    /**
     * 当月出勤天数
     * INT NOT NULL DEFAULT 0
     */
    private Integer totalWorkDays;

    /**
     * 薪酬标准快照（元/小时）
     * DECIMAL(8,2)
     */
    private BigDecimal salaryRate;

    /**
     * 系统核算金额
     * DECIMAL(10,2)
     */
    private BigDecimal calculatedAmount;

    /**
     * 部门确认金额
     * DECIMAL(10,2)
     */
    private BigDecimal confirmedAmount;

    /**
     * 最终审批金额
     * DECIMAL(10,2)
     */
    private BigDecimal finalAmount;

    /**
     * 状态: 1-待部门确认 2-待资助中心审批 3-已审批 4-已发放
     */
    private Integer status;

    /** 部门确认人ID */
    private Long deptConfirmId;

    /** 部门确认时间 */
    private LocalDateTime deptConfirmTime;

    /** 资助中心审批人ID */
    private Long schoolApproveId;

    /** 资助中心审批时间 */
    private LocalDateTime schoolApproveTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除
     * TINYINT(1) DEFAULT 0
     */
    @TableLogic
    @TableField("is_deleted")
    private Integer deleted;
}