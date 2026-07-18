package edu.workstudy.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("gc_work_study_hire")
public class WorkStudyHire {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 报名申请ID */
    private Long applyId;
    /** 岗位ID */
    private Long positionId;
    /** 学生ID */
    private Long studentId;
    /** 录用状态: 1-在岗 2-已调岗 3-主动离岗 4-违规解聘 */
    private Integer hireStatus;
    /** 录用日期 */
    private LocalDate hireDate;
    /** 离岗日期 */
    private LocalDate leaveDate;
    /** 离岗原因 */
    private String leaveReason;
    /** 录用审批人ID */
    private Long approvedBy;
    /** 审批时间 */
    private LocalDateTime approveTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;
}