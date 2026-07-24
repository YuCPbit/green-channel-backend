package edu.greenchannel.gift.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import edu.greenchannel.gift.entity.review.ReviewRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ReviewRecordMapper extends BaseMapper<ReviewRecord> {
}