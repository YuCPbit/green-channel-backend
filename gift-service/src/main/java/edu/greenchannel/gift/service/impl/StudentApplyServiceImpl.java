package edu.greenchannel.gift.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import edu.greenchannel.common.BusinessException;
import edu.greenchannel.gift.dto.review.StudentApplyUpdateDTO;
import edu.greenchannel.gift.entity.StudentApply;
import edu.greenchannel.gift.entity.GiftPackBatch;
import edu.greenchannel.gift.mapper.GiftPackBatchMapper;
import edu.greenchannel.gift.mapper.StudentApplyMapper;
import edu.greenchannel.gift.service.StudentApplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StudentApplyServiceImpl extends ServiceImpl<StudentApplyMapper, StudentApply>
        implements StudentApplyService {

    private final GiftPackBatchMapper giftPackBatchMapper;

    @Override
    public void reSubmitAfterReject(StudentApplyUpdateDTO dto, Long studentId) {
        StudentApply apply = baseMapper.selectById(dto.getId());
        if (apply == null || apply.getIsDeleted().equals(1)) {
            throw new BusinessException(40400, "申请单据不存在或已删除");
        }
        if (!apply.getStudentId().equals(studentId)) {
            throw new BusinessException(40300, "无权修改该申请");
        }
        // 仅驳回状态可修改重提
        if (!apply.getStatus().equals(1)) {
            throw new BusinessException(40900, "仅驳回状态的申请可修改重提交");
        }

        LambdaUpdateWrapper<StudentApply> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(StudentApply::getId, dto.getId())
                .eq(StudentApply::getStudentId, studentId)
                .eq(StudentApply::getStatus, 1)
                .eq(StudentApply::getIsDeleted, 0);
        // 更新可修改字段
        if (dto.getApplyReason() != null) {
            updateWrapper.set(StudentApply::getApplyReason, dto.getApplyReason());
        }
        // 重置为待辅导员审核
        updateWrapper.set(StudentApply::getStatus, 2);
        updateWrapper.set(StudentApply::getUpdateTime, LocalDateTime.now());
        if (baseMapper.update(null, updateWrapper) != 1) {
            throw new BusinessException(40900, "申请状态已变化，请刷新后重试");
        }
    }

    @Override
    public Long submit(StudentApply apply, Long studentId) {
        if (apply.getPackBatchId() == null) {
            throw new BusinessException(40000, "请选择礼包批次");
        }
        GiftPackBatch batch = giftPackBatchMapper.selectById(apply.getPackBatchId());
        if (batch == null || Integer.valueOf(1).equals(batch.getIsDeleted())
                || !Integer.valueOf(1).equals(batch.getStatus())) {
            throw new BusinessException(40900, "礼包批次不存在或未启用");
        }
        if (apply.getApplyReason() == null || apply.getApplyReason().isBlank()) {
            throw new BusinessException(40000, "申请理由不能为空");
        }

        LocalDateTime now = LocalDateTime.now();
        apply.setId(null);
        apply.setStudentId(studentId);
        apply.setApplyNo("GIFT-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase());
        apply.setStatus(2);
        apply.setPickupCode(null);
        apply.setPickupStatus(0);
        apply.setPickupTime(null);
        apply.setPickupOperatorId(null);
        apply.setPickupRemark(null);
        apply.setApplyReason(apply.getApplyReason().trim());
        apply.setApplyTime(now);
        apply.setIsDeleted(0);
        apply.setCreateTime(now);
        apply.setUpdateTime(now);
        try {
            baseMapper.insert(apply);
        } catch (DuplicateKeyException exception) {
            throw new BusinessException(40900, "该礼包批次已经提交过申请");
        }
        return apply.getId();
    }

    @Override
    public StudentApply getMine(Long applyId, Long studentId) {
        StudentApply apply = baseMapper.selectById(applyId);
        if (apply == null || Integer.valueOf(1).equals(apply.getIsDeleted())) {
            throw new BusinessException(40400, "申请不存在");
        }
        if (!studentId.equals(apply.getStudentId())) {
            throw new BusinessException(40300, "无权查看该申请");
        }
        return apply;
    }

    @Override
    public List<StudentApply> listMine(Long studentId, Long packBatchId) {
        return baseMapper.selectList(new LambdaQueryWrapper<StudentApply>()
                .eq(StudentApply::getStudentId, studentId)
                .eq(packBatchId != null, StudentApply::getPackBatchId, packBatchId)
                .eq(StudentApply::getIsDeleted, 0)
                .orderByDesc(StudentApply::getCreateTime));
    }
}
