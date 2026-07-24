package edu.greenchannel.workstudy.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("gc_work_study_batch")
public class WorkStudyBatch {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String batchName;
    private String academicYear;
    private Integer semester; // 1-第一学期 2-第二学期

    private LocalDateTime registerStartTime;
    private LocalDateTime registerEndTime;
    private LocalDateTime interviewStartTime;
    private LocalDateTime interviewEndTime;

    private LocalDate workStartDate;
    private LocalDate workEndDate;

    private Integer maxPositions;
    private Integer status; // 使用 WorkStudyStatus 枚举维护

    private Long creatorId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @TableField("is_deleted")
    private Integer deleted;
}