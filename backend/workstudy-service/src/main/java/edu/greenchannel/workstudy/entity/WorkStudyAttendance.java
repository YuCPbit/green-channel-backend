package edu.greenchannel.workstudy.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("gc_work_study_attendance")
public class WorkStudyAttendance {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 录用记录ID */
    private Long hireId;

    /** 学生ID */
    private Long studentId;

    /** 考勤日期 */
    private LocalDate attendanceDate;

    /** 签到时间 */
    private LocalDateTime checkInTime;

    /** 签退时间 */
    private LocalDateTime checkOutTime;

    /** 签到地点 */
    private String checkInLocation;

    /** 考勤类型：1-打卡 2-请假 3-补卡 */
    private Integer attendanceType;

    /** 请假类型：1-病假 2-事假 3-公假 */
    private Integer leaveType;

    /** 审批状态：0-无需审批 1-待审批 2-已通过 3-已驳回 */
    private Integer approvalStatus;

    /** 审批人ID */
    private Long approverId;

    /** 审批时间 */
    private LocalDateTime approveTime;

    /** 迟到分钟数 */
    private Integer lateMinutes;

    /** 早退分钟数 */
    private Integer earlyMinutes;

    /** 工时（小时） */
    private BigDecimal workHours;

    /** 状态：1-正常 2-迟到 3-迟到+早退 4-请假 5-早退 6-旷工 */
    private Integer status;

    /** 备注（请假/补卡原因） */
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;
}