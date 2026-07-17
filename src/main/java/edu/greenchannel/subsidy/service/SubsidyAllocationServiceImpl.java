package edu.greenchannel.subsidy.service;

import edu.greenchannel.subsidy.dto.request.AllocationCreateRequest;
import edu.greenchannel.subsidy.dto.response.AllocationItemResponse;
import edu.greenchannel.subsidy.dto.response.AllocationSummaryResponse;
import edu.greenchannel.subsidy.entity.College;
import edu.greenchannel.subsidy.entity.SubsidyAllocation;
import edu.greenchannel.subsidy.entity.SubsidyBatch;
import edu.greenchannel.subsidy.enums.BatchStatus;
import edu.greenchannel.subsidy.enums.TargetType;
import edu.greenchannel.subsidy.repository.CollegeRepository;
import edu.greenchannel.subsidy.repository.SubsidyAllocationRepository;
import edu.greenchannel.subsidy.repository.SubsidyBatchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class SubsidyAllocationServiceImpl implements SubsidyAllocationService {

    @Autowired
    private SubsidyAllocationRepository allocationRepository;

    @Autowired
    private SubsidyBatchRepository batchRepository;

    @Autowired
    private CollegeRepository collegeRepository;

    /** 读取批次；若不存在则抛异常 */
    private SubsidyBatch getBatchOrThrow(Long batchId) {
        return batchRepository.findById(batchId)
                .orElseThrow(() -> new IllegalArgumentException("批次不存在！"));
    }

    /** 从批次表读取学校总预算 */
    private BigDecimal getSchoolBudget(Long batchId) {
        return getBatchOrThrow(batchId).getTotalAmount();
    }

    @Override
    @Transactional
    public void allocateQuota(AllocationCreateRequest request, Integer currentUserRole, Long currentUserCollegeId) {
        // 1. 角色权限校验 + 预算控费
        if (request.targetType().equals(TargetType.COLLEGE.getCode())) {
            if (currentUserRole != 1) {
                throw new IllegalStateException("只有学校资助中心有权向学院分配额度！");
            }
            // 学校只能分配可用余额（各学院分配总和不超过学校总预算）
            SubsidyBatch batch = getBatchOrThrow(request.batchId());
            BigDecimal schoolBudget = batch.getTotalAmount();
            if (schoolBudget.compareTo(BigDecimal.ZERO) == 0) {
                throw new IllegalArgumentException("该批次尚未设置总预算，请先在批次配置中填写金额。");
            }
            BigDecimal allCollegesAllocated = allocationRepository.sumAllCollegesAllocationByBatch(request.batchId());
            Optional<SubsidyAllocation> existing = allocationRepository.findByBatchIdAndTargetTypeAndTargetId(
                    request.batchId(), TargetType.COLLEGE.getCode(), request.targetId()
            );
            BigDecimal previousAmount = existing.map(SubsidyAllocation::getAmount).orElse(BigDecimal.ZERO);
            // 学校下发的额度一旦分配不可减少，只能追加
            if (existing.isPresent() && request.amount().compareTo(previousAmount) < 0) {
                throw new IllegalArgumentException("分配失败：已下发给该学院的额度不可减少（当前已分配 " + previousAmount + "），只能追加余额。");
            }
            BigDecimal newTotal = allCollegesAllocated.subtract(previousAmount).add(request.amount());
            if (newTotal.compareTo(schoolBudget) > 0) {
                throw new IllegalArgumentException("分配失败：各学院分配总和(" + newTotal + ")将超出学校总预算(" + schoolBudget + ")！");
            }
        } else if (request.targetType().equals(TargetType.GRADE.getCode())) {
            if (currentUserRole != 2) {
                throw new IllegalStateException("只有学院管理员有权向年级分配额度！");
            }
            // 学院在批次未开始时即可调整年级额度，受学院总额度限制；批次开始后不可减少已下发额度
            SubsidyBatch batch = getBatchOrThrow(request.batchId());
            BigDecimal collegeTotalBudget = allocationRepository.sumAmountByBatchAndCollege(request.batchId(), currentUserCollegeId);
            if (collegeTotalBudget.compareTo(BigDecimal.ZERO) == 0) {
                throw new IllegalArgumentException("该学院尚未获得学校下发的额度，无法向年级分配。");
            }
            BigDecimal collegeAlreadyAllocated = allocationRepository.sumAllocatedByCollegeToGrades(request.batchId(), currentUserCollegeId);
            Optional<SubsidyAllocation> existing = allocationRepository.findByBatchIdAndTargetTypeAndTargetId(
                    request.batchId(), TargetType.GRADE.getCode(), request.targetId()
            );
            BigDecimal previousAmount = existing.map(SubsidyAllocation::getAmount).orElse(BigDecimal.ZERO);
            // 批次开始后，已下发给年级的额度不可减少，只能追加
            if (batch.getStatus() != BatchStatus.DRAFT && existing.isPresent() && request.amount().compareTo(previousAmount) < 0) {
                throw new IllegalArgumentException("分配失败：批次开始后已下发给该年级的额度不可减少（当前已分配 " + previousAmount + "），只能继续下发可分配额度。");
            }
            BigDecimal newAllocatedSum = collegeAlreadyAllocated.subtract(previousAmount).add(request.amount());
            if (newAllocatedSum.compareTo(collegeTotalBudget) > 0) {
                throw new IllegalArgumentException("分配失败：下发给年级的额度总和(" + newAllocatedSum + ")超出了学院分得的预算额度(" + collegeTotalBudget + ")！");
            }
        }

        // 2. 确定 collegeId
        Long collegeId = currentUserRole == 1 ? request.targetId() : currentUserCollegeId;

        // 3. 执行分配写入（已存在则更新，不存在则创建）
        SubsidyAllocation allocation = allocationRepository.findByBatchIdAndTargetTypeAndTargetId(
                request.batchId(), request.targetType(), request.targetId()
        ).orElse(new SubsidyAllocation());

        allocation.setBatchId(request.batchId());
        allocation.setAllocatorRole(currentUserRole);
        allocation.setTargetType(request.targetType());
        allocation.setSourceId(currentUserRole == 1 ? 0L : currentUserCollegeId);
        allocation.setTargetId(request.targetId());
        allocation.setCollegeId(collegeId);
        allocation.setAmount(request.amount());

        if (request.targetType().equals(TargetType.GRADE.getCode())) {
            allocation.setGrade(request.targetId().intValue());
        } else {
            allocation.setGrade(null);
        }

        allocationRepository.save(allocation);
    }

    @Override
    public AllocationSummaryResponse getSummary(Long batchId, Integer currentUserRole, Long currentUserCollegeId) {
        BigDecimal total;
        BigDecimal allocated;

        if (currentUserRole == 1) {
            total = getSchoolBudget(batchId);
            allocated = allocationRepository.sumAllCollegesAllocationByBatch(batchId);
        } else {
            total = allocationRepository.sumAmountByBatchAndCollege(batchId, currentUserCollegeId);
            allocated = allocationRepository.sumAllocatedByCollegeToGrades(batchId, currentUserCollegeId);
        }

        BigDecimal available = total.subtract(allocated);
        return new AllocationSummaryResponse(total, allocated, available);
    }

    @Override
    public List<AllocationItemResponse> listAllocations(Long batchId, Integer targetType) {
        List<SubsidyAllocation> list = targetType != null
                ? allocationRepository.findByBatchIdAndTargetType(batchId, targetType)
                : allocationRepository.findByBatchId(batchId);
        return list.stream().map(a -> new AllocationItemResponse(
                a.getId(), a.getBatchId(), a.getTargetType(), a.getTargetId(),
                a.getCollegeId(), a.getGrade(), a.getAmount(), a.getUsedAmount()
        )).toList();
    }

    @Override
    public List<College> listColleges() {
        return collegeRepository.findByIsDeletedOrderByCollegeNameAsc(0);
    }

    @Override
    public List<Integer> listGrades() {
        return collegeRepository.findDistinctGrades();
    }
}
