package edu.workstudy.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.workstudy.entity.WorkStudyApply;
import edu.workstudy.entity.WorkStudyHire;
import edu.workstudy.entity.WorkStudyPosition;
import edu.workstudy.mapper.WorkStudyHireMapper;
import edu.workstudy.mapper.WorkStudyPositionMapper;
import edu.workstudy.service.WorkStudyApplyService;
import edu.workstudy.service.WorkStudyAgreementService;
import edu.workstudy.service.WorkStudyHireService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkStudyHireServiceImpl
        extends ServiceImpl<WorkStudyHireMapper, WorkStudyHire>
        implements WorkStudyHireService {

    private final WorkStudyApplyService applyService;
    private final WorkStudyPositionMapper positionMapper;
    private final WorkStudyAgreementService agreementService;

    @Override
    @Transactional
    public Long approveHire(Long applyId, Long approverId) {
        WorkStudyApply apply = applyService.getById(applyId);
        if (apply == null || apply.getStatus() != 3) {
            throw new RuntimeException("申请单状态不正确，无法录用");
        }

        WorkStudyPosition position = positionMapper.selectById(apply.getPositionId());
        if (position == null) {
            throw new RuntimeException("岗位不存在");
        }
        if (position.getHiredCount() >= position.getRecruitCount()) {
            throw new RuntimeException("岗位名额已满，无法继续录用");
        }

        WorkStudyHire hire = new WorkStudyHire();
        hire.setApplyId(applyId);
        hire.setPositionId(apply.getPositionId());
        hire.setStudentId(apply.getStudentId());
        hire.setHireStatus(1);
        hire.setHireDate(LocalDate.now());
        hire.setApprovedBy(approverId);
        hire.setApproveTime(LocalDateTime.now());

        hire.setSalaryRate(position.getSalaryRate());

        save(hire);

        apply.setStatus(4);
        applyService.updateById(apply);

        boolean updated = updatePositionHiredCount(position.getId());
        if (!updated) {
            throw new RuntimeException("更新岗位录用人数失败，请重试");
        }

        WorkStudyPosition updatedPosition = positionMapper.selectById(position.getId());
        if (updatedPosition.getHiredCount() >= updatedPosition.getRecruitCount()) {
            updatedPosition.setStatus(3);
            positionMapper.updateById(updatedPosition);
        }

        agreementService.generateAgreement(hire.getId());

        return hire.getId();
    }

    private boolean updatePositionHiredCount(Long positionId) {
        return positionMapper.update(null,
                new UpdateWrapper<WorkStudyPosition>()
                        .eq("id", positionId)
                        .lt("hired_count", "recruit_count")
                        .setSql("hired_count = hired_count + 1")) > 0;
    }
}