package edu.greenchannel.subsidy.service;

import edu.greenchannel.subsidy.dto.request.*;
import edu.greenchannel.subsidy.dto.response.BatchResponse;
import edu.greenchannel.subsidy.entity.SubsidyBatch;
import edu.greenchannel.subsidy.enums.BatchStatus;
import edu.greenchannel.subsidy.repository.SubsidyBatchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class SubsidyBatchServiceImpl implements SubsidyBatchService {

    @Autowired
    private SubsidyBatchRepository batchRepository;

    @Override
    @Transactional
    public BatchResponse createBatch(BatchCreateRequest request) {
        validateBatchTimes(request.applyStartTime(), request.applyEndTime(), request.collegeSubmitEndTime());
        // 集中批次(subsidyType=1)每学年仅允许一个
        if (request.subsidyType() != null && request.subsidyType() == 1) {
            long existingCount = batchRepository.countByAcademicYearAndSubsidyType(request.academicYear(), 1, null);
            if (existingCount > 0) {
                throw new IllegalArgumentException("该学年已存在集中批次，每学年仅允许创建一个集中批次。");
            }
        }

        SubsidyBatch batch = new SubsidyBatch();
        batch.setBatchName(request.batchName());
        batch.setAcademicYear(request.academicYear());
        batch.setSubsidyType(request.subsidyType() != null ? request.subsidyType() : 1);
        batch.setTotalAmount(request.totalAmount() != null ? request.totalAmount() : BigDecimal.ZERO);
        batch.setApplyStartTime(request.applyStartTime());
        batch.setApplyEndTime(request.applyEndTime());
        batch.setCollegeSubmitEndTime(request.collegeSubmitEndTime());
        batch.setStatus(BatchStatus.DRAFT);

        SubsidyBatch saved = batchRepository.save(batch);
        return toResponse(saved);
    }

    @Override
    public Page<BatchResponse> queryBatches(BatchQueryRequest request) {
        Pageable pageable = PageRequest.of(request.page(), request.size(), Sort.by("createTime").descending());
        BatchStatus statusFilter = request.status() != null ? BatchStatus.fromCode(request.status()) : null;
        Page<SubsidyBatch> page = batchRepository.findByCondition(request.batchName(), statusFilter, pageable);
        return page.map(this::toResponse);
    }

    @Override
    @Transactional
    public BatchResponse updateBatch(Long id, BatchUpdateRequest request) {
        SubsidyBatch batch = batchRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("该批次不存在！"));

        validateBatchTimes(request.applyStartTime(), request.applyEndTime(), request.collegeSubmitEndTime());
        // 集中批次(subsidyType=1)每学年仅允许一个（排除自身）
        if (request.subsidyType() != null && request.subsidyType() == 1 && request.academicYear() != null) {
            long existingCount = batchRepository.countByAcademicYearAndSubsidyType(request.academicYear(), 1, id);
            if (existingCount > 0) {
                throw new IllegalArgumentException("该学年已存在集中批次，每学年仅允许创建一个集中批次。");
            }
        }

        batch.setBatchName(request.batchName());
        if (request.academicYear() != null) {
            batch.setAcademicYear(request.academicYear());
        }
        if (request.subsidyType() != null) {
            batch.setSubsidyType(request.subsidyType());
        }
        if (request.totalAmount() != null) {
            batch.setTotalAmount(request.totalAmount());
        }
        batch.setApplyStartTime(request.applyStartTime());
        batch.setApplyEndTime(request.applyEndTime());
        batch.setCollegeSubmitEndTime(request.collegeSubmitEndTime());

        return toResponse(batchRepository.save(batch));
    }

    private void validateBatchTimes(LocalDateTime start, LocalDateTime end, LocalDateTime submitEnd) {
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("申请开始时间不能迟于学生端截止时间！");
        }
        if (end.isAfter(submitEnd)) {
            throw new IllegalArgumentException("学生端截止时间不能迟于学院审核提交截止时间！");
        }
    }

    @Override
    @Transactional
    public BatchResponse startBatch(Long id) {
        SubsidyBatch batch = batchRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("该批次不存在！"));
        if (batch.getStatus() != BatchStatus.DRAFT) {
            throw new IllegalStateException("只有草稿状态的批次可以开始。当前状态：" + batch.getStatus().getDesc());
        }
        if (batch.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("请先设置批次总预算后再开始。");
        }
        batch.setStatus(BatchStatus.ACTIVE);
        return toResponse(batchRepository.save(batch));
    }

    @Override
    @Transactional
    public BatchResponse endBatch(Long id) {
        SubsidyBatch batch = batchRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("该批次不存在！"));
        if (batch.getStatus() != BatchStatus.ACTIVE) {
            throw new IllegalStateException("只有进行中的批次可以提前结束。当前状态：" + batch.getStatus().getDesc());
        }
        batch.setStatus(BatchStatus.ENDED);
        return toResponse(batchRepository.save(batch));
    }

    private BatchResponse toResponse(SubsidyBatch entity) {
        return new BatchResponse(
                entity.getId(),
                entity.getBatchName(),
                entity.getAcademicYear(),
                entity.getSubsidyType(),
                entity.getTotalAmount(),
                entity.getApplyStartTime(),
                entity.getApplyEndTime(),
                entity.getCollegeSubmitEndTime(),
                entity.getStatus().getCode(),
                entity.getCreateTime()
        );
    }
}
