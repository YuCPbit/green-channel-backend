package edu.greenchannel.gift.entity.review;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("gc_review_record")
public class ReviewRecord {
    @TableId(type = IdType.AUTO)
    private Long id;

    // 关联礼包申请单id gc_student_apply.id
    private Long applyId;
    // 单据类型 1礼包 2补助
    private Integer applyType;
    // 审核人用户ID
    private Long reviewerId;
    // 审核层级 1辅导员 2学院 3学校
    private Integer reviewerRole;
    // 操作 1通过 2驳回修改 3不通过 4修改单据
    private Integer action;
    // 审核意见
    private String comment;
    private String modifiedContent;
    // 公共审计字段
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer isDeleted;
}