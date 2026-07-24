package edu.greenchannel.tutor.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 辅导员申请类型配置表
 */
@Entity
@Table(name = "gc_tutor_apply_type")
public class TutorApplyType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "type_name", nullable = false)
    private String typeName;

    @Column(name = "type_code", nullable = false)
    private String typeCode;

    @Column(name = "description")
    private String description;

    @Column(name = "need_amount")
    private Integer needAmount = 0;

    @Column(name = "need_student")
    private Integer needStudent = 1;

    @Column(name = "approval_level")
    private Integer approvalLevel = 2;

    @Column(name = "form_template", columnDefinition = "JSON")
    private String formTemplate;

    @Column(name = "sort")
    private Integer sort = 0;

    @Column(name = "status")
    private Integer status = 1;

    @Column(name = "create_time", insertable = false, updatable = false)
    private LocalDateTime createTime;

    @Column(name = "update_time", insertable = false)
    private LocalDateTime updateTime;

    @Column(name = "is_deleted")
    private Integer isDeleted = 0;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTypeName() { return typeName; }
    public void setTypeName(String typeName) { this.typeName = typeName; }
    public String getTypeCode() { return typeCode; }
    public void setTypeCode(String typeCode) { this.typeCode = typeCode; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getNeedAmount() { return needAmount; }
    public void setNeedAmount(Integer needAmount) { this.needAmount = needAmount; }
    public Integer getNeedStudent() { return needStudent; }
    public void setNeedStudent(Integer needStudent) { this.needStudent = needStudent; }
    public Integer getApprovalLevel() { return approvalLevel; }
    public void setApprovalLevel(Integer approvalLevel) { this.approvalLevel = approvalLevel; }
    public String getFormTemplate() { return formTemplate; }
    public void setFormTemplate(String formTemplate) { this.formTemplate = formTemplate; }
    public Integer getSort() { return sort; }
    public void setSort(Integer sort) { this.sort = sort; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public LocalDateTime getCreateTime() { return createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public Integer getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Integer isDeleted) { this.isDeleted = isDeleted; }
}
