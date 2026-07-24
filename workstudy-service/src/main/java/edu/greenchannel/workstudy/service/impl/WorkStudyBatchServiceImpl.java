package edu.greenchannel.workstudy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import edu.greenchannel.common.BusinessException;
import edu.greenchannel.workstudy.entity.WorkStudyBatch;
import edu.greenchannel.workstudy.enums.WorkStudyStatus;
import edu.greenchannel.workstudy.mapper.WorkStudyBatchMapper;
import edu.greenchannel.workstudy.service.WorkStudyBatchService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class WorkStudyBatchServiceImpl
        extends ServiceImpl<WorkStudyBatchMapper, WorkStudyBatch>
        implements WorkStudyBatchService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createBatch(WorkStudyBatch batch, Long userId) {
        validateBatchParams(batch);

        batch.setCreatorId(userId);
        batch.setStatus(WorkStudyStatus.BATCH_NOT_STARTED.getCode());
        batch.setDeleted(0);
        batch.setCreateTime(LocalDateTime.now());
        batch.setUpdateTime(LocalDateTime.now());

        save(batch);
        return batch.getId();
    }

    @Override
    public List<WorkStudyBatch> listValidBatches() {
        return list(new LambdaQueryWrapper<WorkStudyBatch>()
                .eq(WorkStudyBatch::getDeleted, 0)
                .orderByDesc(WorkStudyBatch::getCreateTime));
    }

    @Override
    public WorkStudyBatch getCurrentBatch() {
        return getOne(new LambdaQueryWrapper<WorkStudyBatch>()
                .eq(WorkStudyBatch::getDeleted, 0)
                .in(WorkStudyBatch::getStatus,
                        WorkStudyStatus.BATCH_REGISTERING.getCode(),
                        WorkStudyStatus.BATCH_INTERVIEWING.getCode(),
                        WorkStudyStatus.BATCH_IN_PROGRESS.getCode())
                .orderByDesc(WorkStudyBatch::getCreateTime)
                .last("LIMIT 1"));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBatchStatus(Long batchId, WorkStudyStatus newStatus) {
        WorkStudyBatch batch = getById(batchId);
        if (batch == null || batch.getDeleted() == 1) {
            throw new BusinessException(40400, "批次不存在");
        }

        WorkStudyStatus currentStatus = WorkStudyStatus.fromCode(batch.getStatus());
        validateStatusTransition(currentStatus, newStatus);

        batch.setStatus(newStatus.getCode());
        batch.setUpdateTime(LocalDateTime.now());
        updateById(batch);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBatch(Long batchId) {
        WorkStudyBatch batch = getById(batchId);
        if (batch == null || batch.getDeleted() == 1) {
            throw new BusinessException(40400, "批次不存在");
        }

        if (batch.getStatus() != null &&
                batch.getStatus() != WorkStudyStatus.BATCH_NOT_STARTED.getCode()) {
            throw new BusinessException(40900, "只能删除未开始的批次");
        }

        batch.setDeleted(1);
        batch.setUpdateTime(LocalDateTime.now());
        updateById(batch);
    }

    /**
     * 状态机校验：定义合法的流转路径
     */
    private void validateStatusTransition(WorkStudyStatus current, WorkStudyStatus target) {
        boolean allowed = switch (current) {
            case BATCH_NOT_STARTED -> target == WorkStudyStatus.BATCH_REGISTERING;
            case BATCH_REGISTERING -> target == WorkStudyStatus.BATCH_INTERVIEWING ||
                    target == WorkStudyStatus.BATCH_FINISHED;
            case BATCH_INTERVIEWING -> target == WorkStudyStatus.BATCH_IN_PROGRESS ||
                    target == WorkStudyStatus.BATCH_FINISHED;
            case BATCH_IN_PROGRESS -> target == WorkStudyStatus.BATCH_FINISHED;
            case BATCH_FINISHED -> false; // 终态
            case POSITION_DRAFT -> false;
            case POSITION_PENDING_APPROVAL -> false;
            case POSITION_ONLINE -> false;
            case POSITION_OFFLINE -> false;
            case POSITION_REJECTED -> false;
            case APPLY_SUBMITTED -> false;
            case APPLY_INTERVIEWING -> false;
            case APPLY_PENDING_APPROVAL -> false;
            case APPLY_HIRED -> false;
            case APPLY_REJECTED -> false;
            case INTERVIEW_PENDING -> false;
            case INTERVIEW_COMPLETED -> false;
            case INTERVIEW_PASSED -> false;
            case INTERVIEW_FAILED -> false;
        };

        if (!allowed) {
            throw new BusinessException(40900,
                    String.format("状态不允许从 [%s] 变更为 [%s]",
                            current.getDesc(), target.getDesc()));
        }
    }

    /**
     * 参数基础校验
     */
    private void validateBatchParams(WorkStudyBatch batch) {
        if (batch.getRegisterEndTime().isBefore(batch.getRegisterStartTime())) {
            throw new BusinessException(40000, "报名结束时间不能早于开始时间");
        }
        // 此处可继续添加其他时间逻辑校验
    }
}