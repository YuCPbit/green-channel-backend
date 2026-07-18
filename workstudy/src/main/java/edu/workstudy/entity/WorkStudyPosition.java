package edu.workstudy.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("gc_work_study_position")
public class WorkStudyPosition {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long batchId;
    private String positionName;
    private String departmentName;
    private Integer recruitCount;
    private Integer hiredCount;
    private Integer status; // 0草稿 1待审 2已上架
    private Long publisherId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}