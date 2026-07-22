package edu.greenchannel.gift.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.greenchannel.gift.entity.GreenChannelBatch;
import edu.greenchannel.gift.mapper.GreenChannelBatchMapper;
import edu.greenchannel.gift.service.GreenChannelBatchService;
import org.springframework.stereotype.Service;

@Service
public class GreenChannelBatchServiceImpl extends ServiceImpl<GreenChannelBatchMapper, GreenChannelBatch>
        implements GreenChannelBatchService {
}