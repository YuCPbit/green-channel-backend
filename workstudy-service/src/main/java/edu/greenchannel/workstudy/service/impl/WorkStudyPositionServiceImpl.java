package edu.greenchannel.workstudy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import edu.greenchannel.common.BusinessException;
import edu.greenchannel.workstudy.entity.WorkStudyBatch;
import edu.greenchannel.workstudy.entity.WorkStudyPosition;
import edu.greenchannel.workstudy.enums.WorkStudyStatus;
import edu.greenchannel.workstudy.mapper.WorkStudyBatchMapper;
import edu.greenchannel.workstudy.mapper.WorkStudyPositionMapper;
import edu.greenchannel.workstudy.service.WorkStudyPositionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class WorkStudyPositionServiceImpl
        extends ServiceImpl<WorkStudyPositionMapper, WorkStudyPosition>
        implements WorkStudyPositionService {

    private final WorkStudyBatchMapper batchMapper;

    public WorkStudyPositionServiceImpl(WorkStudyBatchMapper batchMapper) {
        this.batchMapper = batchMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long publishPosition(WorkStudyPosition position, Long userId) {
        // 1. 校验批次是否存在且有效
        WorkStudyBatch batch = batchMapper.selectById(position.getBatchId());
        if (batch == null || batch.getDeleted() == 1) {
            throw new BusinessException(40400, "所属批次不存在或已删除");
        }

        // 2. 校验批次状态（只有报名中或面试中的批次才能发布岗位）
        Integer batchStatus = batch.getStatus();
        if (batchStatus != WorkStudyStatus.BATCH_REGISTERING.getCode() &&
                batchStatus != WorkStudyStatus.BATCH_INTERVIEWING.getCode()) {
            throw new BusinessException(40900, "当前批次状态不允许发布岗位");
        }

        // 3. 校验薪酬标准（不低于最低工资标准）
        BigDecimal minSalary = new BigDecimal("12.00"); // 后续从系统参数获取
        if (position.getSalaryRate() == null ||
                position.getSalaryRate().compareTo(minSalary) < 0) {
            throw new BusinessException(40000, "薪酬标准不得低于最低工资标准");
        }

        // 4. 校验每周最大工时（不超过系统限制）
        if (position.getMaxWeeklyHours() != null && position.getMaxWeeklyHours() > 8) {
            throw new BusinessException(40000, "每周工时不得超过8小时");
        }

        // 5. 设置默认值
        position.setPublisherId(userId);
        position.setStatus(WorkStudyStatus.POSITION_DRAFT.getCode()); // 草稿状态
        position.setHiredCount(0);
        position.setDeleted(0);
        position.setCreateTime(LocalDateTime.now());
        position.setUpdateTime(LocalDateTime.now());

        save(position);
        return position.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitForApproval(Long positionId, Long userId) {
        WorkStudyPosition position = getById(positionId);
        if (position == null || position.getDeleted() == 1) {
            throw new BusinessException(40400, "岗位不存在");
        }

        // 只有草稿状态的岗位才能提交审核
        if (position.getStatus() != WorkStudyStatus.POSITION_DRAFT.getCode()) {
            throw new BusinessException(40900, "岗位状态不允许提交审核");
        }

        position.setStatus(WorkStudyStatus.POSITION_PENDING_APPROVAL.getCode());
        position.setUpdateTime(LocalDateTime.now());
        updateById(position);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approvePosition(Long positionId, boolean approved, String rejectReason, Long userId) {
        WorkStudyPosition position = getById(positionId);
        if (position == null || position.getDeleted() == 1) {
            throw new BusinessException(40400, "岗位不存在");
        }

        // 只有待审核状态的岗位才能被审核
        if (position.getStatus() != WorkStudyStatus.POSITION_PENDING_APPROVAL.getCode()) {
            throw new BusinessException(40900, "岗位不在待审核状态");
        }

        if (approved) {
            position.setStatus(WorkStudyStatus.POSITION_ONLINE.getCode());
        } else {
            position.setStatus(WorkStudyStatus.POSITION_REJECTED.getCode());
            // 可以在这里保存驳回原因到扩展字段
        }

        position.setUpdateTime(LocalDateTime.now());
        updateById(position);
    }

    @Override
    public List<WorkStudyPosition> listValidPositions(Long batchId, Integer status) {
        LambdaQueryWrapper<WorkStudyPosition> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WorkStudyPosition::getDeleted, 0)
                .orderByDesc(WorkStudyPosition::getCreateTime);

        if (batchId != null) {
            wrapper.eq(WorkStudyPosition::getBatchId, batchId);
        }

        if (status != null) {
            wrapper.eq(WorkStudyPosition::getStatus, status);
        }

        return list(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void offlinePosition(Long positionId, Long userId) {
        WorkStudyPosition position = getById(positionId);
        if (position == null || position.getDeleted() == 1) {
            throw new BusinessException(40400, "岗位不存在");
        }

        // 只有已上架的岗位才能下架
        if (position.getStatus() != WorkStudyStatus.POSITION_ONLINE.getCode()) {
            throw new BusinessException(40900, "只有已上架的岗位才能下架");
        }

        position.setStatus(WorkStudyStatus.POSITION_OFFLINE.getCode());
        position.setUpdateTime(LocalDateTime.now());
        updateById(position);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePosition(WorkStudyPosition position, Long userId) {
        WorkStudyPosition existing = getById(position.getId());
        if (existing == null || existing.getDeleted() == 1) {
            throw new BusinessException(40400, "岗位不存在");
        }

        // 只有草稿或驳回状态的岗位才能修改
        if (existing.getStatus() != WorkStudyStatus.POSITION_DRAFT.getCode() &&
                existing.getStatus() != WorkStudyStatus.POSITION_REJECTED.getCode()) {
            throw new BusinessException(40900, "当前状态不允许修改岗位信息");
        }

        // 更新允许修改的字段
        existing.setPositionName(position.getPositionName());
        existing.setDescription(position.getDescription());
        existing.setWorkLocation(position.getWorkLocation());
        existing.setWorkTimeDesc(position.getWorkTimeDesc());
        existing.setMaxWeeklyHours(position.getMaxWeeklyHours());
        existing.setRequirements(position.getRequirements());
        existing.setContactName(position.getContactName());
        existing.setContactPhone(position.getContactPhone());
        existing.setUpdateTime(LocalDateTime.now());

        updateById(existing);
    }
}