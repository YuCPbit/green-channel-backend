package edu.greenchannel.workstudy.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("gc_work_study_position")
public class WorkStudyPosition {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long batchId;
    private String positionName;
    private String departmentName;
    private Long departmentId;
    private String description;
    private String workLocation;
    private String workTimeDesc;
    private Integer maxWeeklyHours;
    private Integer positionType;
    private Integer recruitCount;
    private Integer hiredCount;
    private Integer salaryType;
    private BigDecimal salaryRate;
    private String requirements;
    private String contactName;
    private String contactPhone;
    private Integer status; // 0-草稿 1-待审核 2-已上架 3-已下架 4-审核不通过
    private Long publisherId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer isDeleted;
}