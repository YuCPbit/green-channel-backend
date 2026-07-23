package edu.greenchannel.gift.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("gc_gift_pack_batch")
public class GiftPackBatch {
    @TableId(type = IdType.AUTO)
    private Long id;

    // 关联的绿色通道批次ID
    private Long gcBatchId;

    // 大礼包批次名称
    private String batchName;

    // 学生可选物品个数上限
    private Integer maxItems;

    // 状态: 0-禁用 1-启用
    private Integer status;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;
}