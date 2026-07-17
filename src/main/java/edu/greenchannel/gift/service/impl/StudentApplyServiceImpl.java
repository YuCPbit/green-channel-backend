package edu.greenchannel.gift.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.greenchannel.gift.entity.StudentApply;
import edu.greenchannel.gift.mapper.StudentApplyMapper;
import edu.greenchannel.gift.service.StudentApplyService;
import org.springframework.stereotype.Service;

@Service
public class StudentApplyServiceImpl extends ServiceImpl<StudentApplyMapper, StudentApply>
        implements StudentApplyService {
}