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

    /**
     * 评分(1-5分)
     */
    private Integer score;

    private String comment;
    private Long evaluatorId;
    private LocalDateTime evalTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @TableField("is_deleted")
    private Integer deleted;
}