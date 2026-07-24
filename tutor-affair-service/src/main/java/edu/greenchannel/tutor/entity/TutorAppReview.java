package edu.greenchannel.tutor.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 辅导员申请审核记录表
 */
@Entity
@Table(name = "gc_tutor_app_review")
public class TutorAppReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "application_id", nullable = false)
    private Long applicationId;

    @Column(name = "reviewer_id", nullable = false)
    private Long reviewerId;

    @Column(name = "reviewer_role", nullable = false)
    private Integer reviewerRole;

    @Column(name = "action", nullable = false)
    private Integer action;

    @Column(name = "comment")
    private String comment;

    @Column(name = "review_time", insertable = false, updatable = false)
    private LocalDateTime reviewTime;

    @Column(name = "create_time", insertable = false, updatable = false)
    private LocalDateTime createTime;

    @Column(name = "update_time", insertable = false)
    private LocalDateTime updateTime;

    @Column(name = "is_deleted")
    private Integer isDeleted = 0;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getApplicationId() { return applicationId; }
    public void setApplicationId(Long applicationId) { this.applicationId = applicationId; }
    public Long getReviewerId() { return reviewerId; }
    public void setReviewerId(Long reviewerId) { this.reviewerId = reviewerId; }
    public Integer getReviewerRole() { return reviewerRole; }
    public void setReviewerRole(Integer reviewerRole) { this.reviewerRole = reviewerRole; }
    public Integer getAction() { return action; }
    public void setAction(Integer action) { this.action = action; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public LocalDateTime getReviewTime() { return reviewTime; }
    public LocalDateTime getCreateTime() { return createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public Integer getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Integer isDeleted) { this.isDeleted = isDeleted; }
}
