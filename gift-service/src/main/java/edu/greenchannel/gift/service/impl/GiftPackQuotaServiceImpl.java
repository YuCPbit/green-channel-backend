package edu.greenchannel.gift.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import edu.greenchannel.gift.entity.GiftPackQuota;
import edu.greenchannel.gift.mapper.GiftPackQuotaMapper;
import edu.greenchannel.gift.service.GiftPackQuotaService;
import org.springframework.stereotype.Service;

@Service
public class GiftPackQuotaServiceImpl extends ServiceImpl<GiftPackQuotaMapper, GiftPackQuota>
        implements GiftPackQuotaService {
}