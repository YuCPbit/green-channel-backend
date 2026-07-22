package edu.greenchannel.gift.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("gc_gift_pack_apply")
public class StudentApply {
    public static final int PICKUP_PENDING = 0;
    public static final int PICKUP_COMPLETED = 1;
    public static final int PICKUP_EXCEPTION = 2;
    public static final int PICKUP_REISSUED = 3;

    @TableId(type = IdType.AUTO)
    private Long id;

    // 礼包批次ID 对应数据库 pack_batch_id
    private Long packBatchId;
    // 学生表主键ID student_id
    private Long studentId;
    // 申请唯一编号 apply_no
    private String applyNo;
    // 申请理由 apply_reason
    private String applyReason;
    // 申请状态 status
    private Integer status;
    // 领取码
    private String pickupCode;
    // 领取状态：0待领取 1已领取 2异常待处理 3已补发
    private Integer pickupStatus;
    // 实际领取时间
    private LocalDateTime pickupTime;
    // 核销工作人员ID
    private Long pickupOperatorId;
    // 领取异常备注
    private String pickupRemark;

    // 申请提交时间
    private LocalDateTime applyTime;
    // 逻辑删除标记
    @TableLogic
    private Integer isDeleted;
    // 创建、更新时间由数据库自动填充，移除fill注解
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
