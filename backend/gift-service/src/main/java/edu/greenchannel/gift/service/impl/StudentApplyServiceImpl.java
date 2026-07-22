package edu.greenchannel.gift.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.greenchannel.common.BusinessException;
import edu.greenchannel.gift.entity.StudentApply;
import edu.greenchannel.gift.mapper.StudentApplyMapper;
import edu.greenchannel.gift.service.StudentApplyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class StudentApplyServiceImpl extends ServiceImpl<StudentApplyMapper, StudentApply>
        implements StudentApplyService {

    @Override
    @Transactional
    public StudentApply pickup(String pickupCode, Long operatorId, String remark) {
        String normalizedCode = pickupCode == null ? "" : pickupCode.trim();
        if (normalizedCode.isEmpty()) {
            throw new BusinessException(40000, "领取码不能为空");
        }

        StudentApply apply = findByPickupCode(normalizedCode);
        if (apply == null) {
            throw new BusinessException(40400, "领取码不存在");
        }
        if (!Integer.valueOf(StudentApply.PICKUP_PENDING).equals(apply.getPickupStatus())) {
            throw new BusinessException(40900, "该礼包已核销或正在异常处理中");
        }

        LocalDateTime pickupTime = LocalDateTime.now();
        boolean updated = markPickedUp(apply.getId(), operatorId, remark, pickupTime);
        if (!updated) {
            throw new BusinessException(40900, "该礼包已被其他工作人员核销，请勿重复操作");
        }

        apply.setPickupStatus(StudentApply.PICKUP_COMPLETED);
        apply.setPickupOperatorId(operatorId);
        apply.setPickupRemark(remark);
        apply.setPickupTime(pickupTime);
        return apply;
    }

    protected StudentApply findByPickupCode(String pickupCode) {
        return getOne(Wrappers.<StudentApply>lambdaQuery()
                .eq(StudentApply::getPickupCode, pickupCode));
    }

    protected boolean markPickedUp(Long applyId, Long operatorId, String remark, LocalDateTime pickupTime) {
        return lambdaUpdate()
                .eq(StudentApply::getId, applyId)
                .eq(StudentApply::getPickupStatus, StudentApply.PICKUP_PENDING)
                .set(StudentApply::getPickupStatus, StudentApply.PICKUP_COMPLETED)
                .set(StudentApply::getPickupOperatorId, operatorId)
                .set(StudentApply::getPickupRemark, remark)
                .set(StudentApply::getPickupTime, pickupTime)
                .update();
    }
}
