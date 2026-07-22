package edu.greenchannel.gift.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.greenchannel.gift.entity.GiftPackBatch;
import edu.greenchannel.gift.mapper.GiftPackBatchMapper;
import edu.greenchannel.gift.service.GiftPackBatchService;
import org.springframework.stereotype.Service;

@Service
public class GiftPackBatchServiceImpl extends ServiceImpl<GiftPackBatchMapper, GiftPackBatch>
        implements GiftPackBatchService {
}