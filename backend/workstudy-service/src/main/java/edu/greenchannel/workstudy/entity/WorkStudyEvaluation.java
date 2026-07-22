package edu.greenchannel.workstudy.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("gc_work_study_evaluation")
public class WorkStudyEvaluation {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long hireId;
    private Long studentId;
    private Integer evalYear;
    private Integer evalMonth;
    private Integer score; // 1-5分
    private String comment;
    private Long evaluatorId; // 评价人（用工部门领导）
    private LocalDateTime evalTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer isDeleted;
}