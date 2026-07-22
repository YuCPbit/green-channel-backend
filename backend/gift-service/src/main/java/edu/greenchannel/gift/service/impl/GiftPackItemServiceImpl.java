package edu.greenchannel.gift.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.greenchannel.gift.entity.GiftPackItem;
import edu.greenchannel.gift.mapper.GiftPackItemMapper;
import edu.greenchannel.gift.service.GiftPackItemService;
import org.springframework.stereotype.Service;

@Service
public class GiftPackItemServiceImpl extends ServiceImpl<GiftPackItemMapper, GiftPackItem>
        implements GiftPackItemService {
}