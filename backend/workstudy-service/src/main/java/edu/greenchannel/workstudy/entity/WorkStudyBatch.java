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
    private Integer semester;
    private LocalDateTime registerStartTime;
    private LocalDateTime registerEndTime;
    private LocalDateTime interviewStartTime;
    private LocalDateTime interviewEndTime;
    private LocalDate workStartDate;
    private LocalDate workEndDate;
    private Integer maxPositions;
    private Integer status; // 0-未开始 1-报名中 2-面试中 3-进行中 4-已结束
    private Long creatorId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer isDeleted;
}