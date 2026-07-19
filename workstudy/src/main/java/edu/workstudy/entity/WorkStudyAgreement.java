package edu.workstudy.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("gc_work_study_agreement")
public class WorkStudyAgreement {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long hireId;
    private Long studentId;
    private Long positionId;
    private String agreementNo;
    private String templateContent; // 存储HTML或Markdown格式的协议快照
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer signStatus; // 0-待签署 1-已签署 2-已到期 3-已续签
    private LocalDateTime studentSignTime;
    private Integer renewCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer isDeleted;
}