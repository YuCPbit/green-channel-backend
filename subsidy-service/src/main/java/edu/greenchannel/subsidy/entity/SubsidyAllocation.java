package edu.greenchannel.subsidy.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "gc_subsidy_allocation")
public class SubsidyAllocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "batch_id", nullable = false)
    private Long batchId;

    @Column(name = "allocator_role", nullable = false)
    private Integer allocatorRole;

    @Column(name = "target_type", nullable = false)
    private Integer targetType;

    @Column(name = "source_id", nullable = false)
    private Long sourceId = 0L;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Column(name = "college_id", nullable = false)
    private Long collegeId;

    @Column(name = "grade")
    private Integer grade;

    @Column(name = "allocated_amount", nullable = false)
    private BigDecimal amount = BigDecimal.ZERO;

    @Column(name = "used_amount")
    private BigDecimal usedAmount = BigDecimal.ZERO;

    @Column(name = "create_time", insertable = false, updatable = false)
    private LocalDateTime createTime;

    @Column(name = "update_time", insertable = false)
    private LocalDateTime updateTime;

    @Column(name = "is_deleted")
    private Integer isDeleted = 0;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }
    public Integer getAllocatorRole() { return allocatorRole; }
    public void setAllocatorRole(Integer allocatorRole) { this.allocatorRole = allocatorRole; }
    public Integer getTargetType() { return targetType; }
    public void setTargetType(Integer targetType) { this.targetType = targetType; }
    public Long getSourceId() { return sourceId; }
    public void setSourceId(Long sourceId) { this.sourceId = sourceId; }
    public Long getTargetId() { return targetId; }
    public void setTargetId(Long targetId) { this.targetId = targetId; }
    public Long getCollegeId() { return collegeId; }
    public void setCollegeId(Long collegeId) { this.collegeId = collegeId; }
    public Integer getGrade() { return grade; }
    public void setGrade(Integer grade) { this.grade = grade; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public BigDecimal getUsedAmount() { return usedAmount; }
    public void setUsedAmount(BigDecimal usedAmount) { this.usedAmount = usedAmount; }
    public LocalDateTime getCreateTime() { return createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public Integer getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Integer isDeleted) { this.isDeleted = isDeleted; }
}
