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

    /** 工作时长(小时) */
    private BigDecimal workHours;

    /** 打卡方式: 1-定位打卡 2-二维码扫码 */
    private Integer checkType;

    /** 签到定位信息 */
    private String checkInLocation;

    /** 状态: 1-正常 2-迟到 3-早退 4-请假 5-旷工 6-补打卡待审批 7-补打卡已通过 */
    private Integer status;

    /** 备注 */
    private String remark;

    /** 用工部门确认人ID */
    private Long confirmedBy;

    /** 确认时间 */
    private LocalDateTime confirmTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @TableField("is_deleted")
    private Integer deleted;
}