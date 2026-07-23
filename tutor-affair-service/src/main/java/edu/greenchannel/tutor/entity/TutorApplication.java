package edu.greenchannel.tutor.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 辅导员申请主表
 */
@Entity
@Table(name = "gc_tutor_application")
public class TutorApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "apply_no", nullable = false)
    private String applyNo;

    @Column(name = "type_id", nullable = false)
    private Long typeId;

    @Column(name = "tutor_id", nullable = false)
    private Long tutorId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "urgency")
    private Integer urgency = 1;

    @Column(name = "status")
    private Integer status = 1;

    @Column(name = "form_data", columnDefinition = "JSON")
    private String formData;

    @Column(name = "apply_time", insertable = false, updatable = false)
    private LocalDateTime applyTime;

    @Column(name = "submit_time")
    private LocalDateTime submitTime;

    @Column(name = "remark")
    private String remark;

    @Column(name = "disburse_status")
    private Integer disburseStatus = 0;

    @Column(name = "disburse_time")
    private LocalDateTime disburseTime;

    @Column(name = "disburse_operator_id")
    private Long disburseOperatorId;

    @Column(name = "create_time", insertable = false, updatable = false)
    private LocalDateTime createTime;

    @Column(name = "update_time", insertable = false)
    private LocalDateTime updateTime;

    @Column(name = "is_deleted")
    private Integer isDeleted = 0;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getApplyNo() { return applyNo; }
    public void setApplyNo(String applyNo) { this.applyNo = applyNo; }
    public Long getTypeId() { return typeId; }
    public void setTypeId(Long typeId) { this.typeId = typeId; }
    public Long getTutorId() { return tutorId; }
    public void setTutorId(Long tutorId) { this.tutorId = tutorId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public Integer getUrgency() { return urgency; }
    public void setUrgency(Integer urgency) { this.urgency = urgency; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getFormData() { return formData; }
    public void setFormData(String formData) { this.formData = formData; }
    public LocalDateTime getApplyTime() { return applyTime; }
    public LocalDateTime getSubmitTime() { return submitTime; }
    public void setSubmitTime(LocalDateTime submitTime) { this.submitTime = submitTime; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public Integer getDisburseStatus() { return disburseStatus; }
    public void setDisburseStatus(Integer disburseStatus) { this.disburseStatus = disburseStatus; }
    public LocalDateTime getDisburseTime() { return disburseTime; }
    public void setDisburseTime(LocalDateTime disburseTime) { this.disburseTime = disburseTime; }
    public Long getDisburseOperatorId() { return disburseOperatorId; }
    public void setDisburseOperatorId(Long disburseOperatorId) { this.disburseOperatorId = disburseOperatorId; }
    public LocalDateTime getCreateTime() { return createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public Integer getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Integer isDeleted) { this.isDeleted = isDeleted; }
}
