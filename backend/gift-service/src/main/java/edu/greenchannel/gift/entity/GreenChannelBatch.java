package edu.greenchannel.gift.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("gc_green_channel_batch")
public class GreenChannelBatch {
    @TableId(type = IdType.AUTO)
    private Long id;

    // 批次名称
    private String batchName;
    // 学年
    private String academicYear;
    // 学期（表必填，之前缺失）
    private Integer semester;
    // 申请开始时间
    private LocalDateTime applyStartTime;
    // 申请截止时间
    private LocalDateTime applyEndTime;
    // 学院提交截止时间
    private LocalDateTime collegeSubmitEndTime;
    // 资金来源
    private String fundSource;
    // 创建人ID
    private Long creatorId;
    // 批次状态 0未开启 1开启 2已结束
    private Integer status;
    // 备注
    private String remark;

    // 逻辑删除
    @TableLogic
    private Integer isDeleted;
    // 创建时间（数据库自带默认值，删除fill注解）
    private LocalDateTime createTime;
    // 更新时间（数据库自动更新）
    private LocalDateTime updateTime;
}