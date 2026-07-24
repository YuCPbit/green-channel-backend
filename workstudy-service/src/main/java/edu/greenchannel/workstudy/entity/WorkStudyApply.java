package edu.greenchannel.workstudy.entity;

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
    private String availableTime;
    private String skills;
    private String applyReason;
    private String tutorRecommend;
    private Integer interviewStatus; // 0-3
    private Integer status; // 1-5
    private LocalDateTime applyTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableLogic
    @TableField("is_deleted")
    private Integer deleted; // TINYINT(1)
}