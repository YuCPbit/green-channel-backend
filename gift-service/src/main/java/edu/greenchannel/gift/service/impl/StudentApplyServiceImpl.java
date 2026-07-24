package edu.greenchannel.gift.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import edu.greenchannel.common.BusinessException;
import edu.greenchannel.gift.dto.review.StudentApplyUpdateDTO;
import edu.greenchannel.gift.entity.StudentApply;
import edu.greenchannel.gift.mapper.StudentApplyMapper;
import edu.greenchannel.gift.service.StudentApplyService;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class StudentApplyServiceImpl extends ServiceImpl<StudentApplyMapper, StudentApply>
        implements StudentApplyService {

    @Override
    public void reSubmitAfterReject(StudentApplyUpdateDTO dto) {
        StudentApply apply = baseMapper.selectById(dto.getId());
        if (apply == null || apply.getIsDeleted().equals(1)) {
            throw new BusinessException(500, "申请单据不存在或已删除");
        }
        // 仅驳回状态可修改重提
        if (!apply.getStatus().equals(1)) {
            throw new BusinessException(500, "仅驳回状态的申请可修改重提交");
        }

        LambdaUpdateWrapper<StudentApply> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(StudentApply::getId, dto.getId());
        // 更新可修改字段
        if (dto.getApplyReason() != null) {
            updateWrapper.set(StudentApply::getApplyReason, dto.getApplyReason());
        }
        // 重置为待辅导员审核
        updateWrapper.set(StudentApply::getStatus, 2);
        updateWrapper.set(StudentApply::getUpdateTime, LocalDateTime.now());
        baseMapper.update(null, updateWrapper);
    }
}