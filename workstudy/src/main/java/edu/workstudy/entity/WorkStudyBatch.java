package edu.workstudy.entity;

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
    private Integer semester;

    private LocalDateTime registerStartTime;
    private LocalDateTime registerEndTime;

    private LocalDate workStartDate;
    private LocalDate workEndDate;

    private Integer maxPositions;
    private Integer status; // 0未开始 1报名中 2进行中 3已结束
    private Long creatorId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;
}