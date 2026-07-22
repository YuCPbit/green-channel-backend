package edu.greenchannel.gift.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("gc_gift_pack_quota")
public class GiftPackQuota {
    @TableId(type = IdType.AUTO)
    private Long id;

    // 大礼包批次ID pack_batch_id
    private Long packBatchId;
    // 学院ID college_id
    private Long collegeId;
    // 年级 grade
    private Integer grade;
    // 分配总名额 allocated_quota
    private Integer allocatedQuota;
    // 已使用名额 used_quota
    private Integer usedQuota;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;
}