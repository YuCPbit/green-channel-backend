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
    private String availableTime;
    private String skills;
    private String applyReason;
    private String tutorRecommend;
    private Integer interviewStatus; // 0-待面试 1-已面试 2-面试通过 3-面试不通过
    private Integer status; // 1-已报名 2-面试中 3-待录用审批 4-已录用 5-未录用
    private LocalDateTime applyTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer isDeleted;
}