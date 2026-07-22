package edu.greenchannel.gift.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("gc_gift_pack_item")
public class GiftPackItem {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("pack_batch_id")
    private Long packBatchId;

    @TableField("item_name")
    private String giftName;

    @TableField("image_url")
    private String imageUrl;

    @TableField("item_type")
    private String itemType;

    @TableField("size_options")
    private String sizeOptions;

    @TableField("introduction")
    private String description;

    @TableField("unit_price")
    private BigDecimal price;

    @TableField("gender_limit")
    private Integer genderLimit;

    @TableField("is_required")
    private Integer isRequired;

    @TableField("inventory")
    private Integer stock;

    private Integer sort;

    // 自动填充时间字段（之前插入为null的问题）
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    // 逻辑删除
    @TableLogic
    private Integer isDeleted;
}