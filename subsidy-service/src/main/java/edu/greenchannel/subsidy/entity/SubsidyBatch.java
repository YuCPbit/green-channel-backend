package edu.greenchannel.subsidy.entity;

import edu.greenchannel.subsidy.enums.BatchStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "gc_subsidy_batch")
public class SubsidyBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "batch_name", nullable = false)
    private String batchName;

    @Column(name = "academic_year", nullable = false)
    private String academicYear;

    @Column(name = "subsidy_type", nullable = false)
    private Integer subsidyType;

    @Column(name = "total_amount")
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "creator_id")
    private Long creatorId;

    @Column(name = "apply_start_time", nullable = false)
    private LocalDateTime applyStartTime;

    @Column(name = "apply_end_time", nullable = false)
    private LocalDateTime applyEndTime;

    @Column(name = "college_submit_end_time", nullable = false)
    private LocalDateTime collegeSubmitEndTime;

    @Enumerated(EnumType.ORDINAL)
    private BatchStatus status = BatchStatus.DRAFT;

    @Column(name = "create_time", insertable = false, updatable = false)
    private LocalDateTime createTime;

    @Column(name = "update_time", insertable = false)
    private LocalDateTime updateTime;

    @Column(name = "is_deleted")
    private Integer isDeleted = 0;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getBatchName() { return batchName; }
    public void setBatchName(String batchName) { this.batchName = batchName; }
    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }
    public Integer getSubsidyType() { return subsidyType; }
    public void setSubsidyType(Integer subsidyType) { this.subsidyType = subsidyType; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public Long getCreatorId() { return creatorId; }
    public void setCreatorId(Long creatorId) { this.creatorId = creatorId; }
    public LocalDateTime getApplyStartTime() { return applyStartTime; }
    public void setApplyStartTime(LocalDateTime applyStartTime) { this.applyStartTime = applyStartTime; }
    public LocalDateTime getApplyEndTime() { return applyEndTime; }
    public void setApplyEndTime(LocalDateTime applyEndTime) { this.applyEndTime = applyEndTime; }
    public LocalDateTime getCollegeSubmitEndTime() { return collegeSubmitEndTime; }
    public void setCollegeSubmitEndTime(LocalDateTime collegeSubmitEndTime) { this.collegeSubmitEndTime = collegeSubmitEndTime; }
    public BatchStatus getStatus() { return status; }
    public void setStatus(BatchStatus status) { this.status = status; }
    public LocalDateTime getCreateTime() { return createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public Integer getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Integer isDeleted) { this.isDeleted = isDeleted; }
}
