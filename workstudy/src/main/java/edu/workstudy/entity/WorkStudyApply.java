package edu.workstudy.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("gc_work_study_apply")
public class WorkStudyApply {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long positionId;
    private Long studentId;
    private String applyNo;
    private String selfIntro;
    private Integer interviewStatus; // 0-待面试 1-已面试 2-通过 3-不通过
    private Integer status; // 1-已报名 2-面试中 3-待录用 4-已录用 5-未录用
    private LocalDateTime applyTime;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer isDeleted;
}