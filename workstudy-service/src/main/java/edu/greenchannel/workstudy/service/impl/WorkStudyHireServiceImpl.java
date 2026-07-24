package edu.greenchannel.workstudy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import edu.greenchannel.common.BusinessException;
import edu.greenchannel.workstudy.entity.WorkStudyApply;
import edu.greenchannel.workstudy.entity.WorkStudyBatch;
import edu.greenchannel.workstudy.entity.WorkStudyHire;
import edu.greenchannel.workstudy.entity.WorkStudyPosition;
import edu.greenchannel.workstudy.enums.WorkStudyStatus;
import edu.greenchannel.workstudy.mapper.WorkStudyBatchMapper;
import edu.greenchannel.workstudy.mapper.WorkStudyHireMapper;
import edu.greenchannel.workstudy.mapper.WorkStudyPositionMapper;
import edu.greenchannel.workstudy.service.WorkStudyAgreementService;
import edu.greenchannel.workstudy.service.WorkStudyApplyService;
import edu.greenchannel.workstudy.service.WorkStudyHireService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkStudyHireServiceImpl
        extends ServiceImpl<WorkStudyHireMapper, WorkStudyHire>
        implements WorkStudyHireService {

    private final WorkStudyApplyService applyService;
    private final WorkStudyPositionMapper positionMapper;
    private final WorkStudyAgreementService agreementService;
    private final WorkStudyBatchMapper batchMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long approveHire(Long applyId, Long approverId) {
        WorkStudyApply apply = applyService.getById(applyId);
        if (apply == null || apply.getDeleted() == 1) {
            throw new BusinessException(40400, "申请记录不存在");
        }

        if (apply.getStatus() == null ||
                apply.getStatus() != WorkStudyStatus.APPLY_PENDING_APPROVAL.getCode()) {
            throw new BusinessException(40000, "申请单状态不正确，无法录用");
        }

        WorkStudyPosition position = positionMapper.selectById(apply.getPositionId());
        if (position == null || position.getDeleted() == 1) {
            throw new BusinessException(40000, "岗位不存在");
        }

        // 1. 固定岗校验（必须放在 incrementHiredCount 之前）
        if (position.getPositionType() != null && position.getPositionType() == 1) {
            boolean hasFixedJob = lambdaQuery()
                    .eq(WorkStudyHire::getStudentId, apply.getStudentId())
                    .eq(WorkStudyHire::getHireStatus, 1) // 1-在岗
                    .eq(WorkStudyHire::getDeleted, 0)
                    .inSql(
                            WorkStudyHire::getPositionId,
                            "SELECT id FROM gc_work_study_position WHERE position_type = 1 AND is_deleted = 0"
                    )
                    .exists();

            if (hasFixedJob) {
                throw new BusinessException(40900, "该生当前已持有固定岗位，无法重复录用");
            }
        }

        // 2. 全校岗位录用总数上限校验
        WorkStudyBatch batch = batchMapper.selectById(position.getBatchId());
        if (batch == null || batch.getDeleted() == 1) {
            throw new BusinessException(40000, "所属批次不存在");
        }

        Integer totalHiredCount = Math.toIntExact(count(
                new LambdaQueryWrapper<WorkStudyHire>()
                        .inSql(
                                WorkStudyHire::getPositionId,
                                "SELECT id FROM gc_work_study_position WHERE batch_id = " + batch.getId() + " AND is_deleted = 0"
                        )
                        .eq(WorkStudyHire::getHireStatus, 1)
                        .eq(WorkStudyHire::getDeleted, 0)
        ));

        if (totalHiredCount >= batch.getMaxPositions()) {
            throw new BusinessException(40900, "全校岗位录用总数已达批次上限，无法继续录用");
        }

        // 3. 岗位名额校验
        boolean success = incrementHiredCount(position.getId());
        if (!success) {
            throw new BusinessException(40900, "岗位名额已满，无法继续录用");
        }

        WorkStudyPosition freshPosition = positionMapper.selectById(position.getId());
        if (freshPosition.getHiredCount() >= freshPosition.getRecruitCount()) {
            freshPosition.setStatus(WorkStudyStatus.POSITION_OFFLINE.getCode());
            positionMapper.updateById(freshPosition);
        }

        WorkStudyHire hire = new WorkStudyHire();
        hire.setApplyId(applyId);
        hire.setPositionId(apply.getPositionId());
        hire.setStudentId(apply.getStudentId());
        hire.setHireStatus(1);
        hire.setHireDate(LocalDate.now());
        hire.setSalaryRate(position.getSalaryRate());
        hire.setApprovedBy(approverId);
        hire.setApproveTime(LocalDateTime.now());
        hire.setDeleted(0);
        hire.setCreateTime(LocalDateTime.now());
        hire.setUpdateTime(LocalDateTime.now());

        save(hire);

        apply.setStatus(WorkStudyStatus.APPLY_HIRED.getCode());
        apply.setUpdateTime(LocalDateTime.now());
        applyService.updateById(apply);

        agreementService.generateAgreement(hire);


        log.info("录用成功: hireId={}, studentId={}, positionId={}",
                hire.getId(), hire.getStudentId(), hire.getPositionId());

        return hire.getId();
    }

    private boolean incrementHiredCount(Long positionId) {
        UpdateWrapper<WorkStudyPosition> wrapper = new UpdateWrapper<>();
        wrapper.eq("id", positionId)
                .lt("hired_count", "recruit_count")
                .setSql("hired_count = hired_count + 1");
        return positionMapper.update(null, wrapper) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void leavePosition(Long hireId, Integer leaveType, String reason, Long operatorId) {
        WorkStudyHire hire = getById(hireId);
        if (hire == null || hire.getDeleted() == 1) {
            throw new BusinessException(40400, "录用记录不存在");
        }

        if (hire.getHireStatus() != 1) {
            throw new BusinessException(40900, "当前状态不允许离岗");
        }

        if (leaveType < 2 || leaveType > 4) {
            throw new BusinessException(40000, "无效的离岗类型");
        }

        hire.setHireStatus(leaveType);
        hire.setLeaveDate(LocalDate.now());
        hire.setLeaveReason(reason);
        hire.setUpdateTime(LocalDateTime.now());
        updateById(hire);

        decrementHiredCount(hire.getPositionId());
    }

    private void decrementHiredCount(Long positionId) {
        UpdateWrapper<WorkStudyPosition> wrapper = new UpdateWrapper<>();
        wrapper.eq("id", positionId)
                .gt("hired_count", 0)
                .setSql("hired_count = hired_count - 1");
        positionMapper.update(null, wrapper);

        WorkStudyPosition position = positionMapper.selectById(positionId);
        if (position.getStatus() == WorkStudyStatus.POSITION_OFFLINE.getCode()
                && position.getHiredCount() < position.getRecruitCount()) {
            position.setStatus(WorkStudyStatus.POSITION_ONLINE.getCode());
            positionMapper.updateById(position);
        }
    }
}