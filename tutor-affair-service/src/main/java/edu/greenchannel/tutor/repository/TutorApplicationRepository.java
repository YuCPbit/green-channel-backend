package edu.greenchannel.tutor.repository;

import edu.greenchannel.tutor.entity.TutorApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 辅导员申请主表 Repository
 */
public interface TutorApplicationRepository extends JpaRepository<TutorApplication, Long> {

    /**
     * 辅导员查询自己的申请
     */
    @Query("SELECT a FROM TutorApplication a WHERE a.tutorId = :tutorId " +
            "AND (:status IS NULL OR a.status = :status) " +
            "AND (:typeId IS NULL OR a.typeId = :typeId) " +
            "AND a.isDeleted = 0 ORDER BY a.createTime DESC")
    Page<TutorApplication> findByTutorId(@Param("tutorId") Long tutorId,
                                         @Param("status") Integer status,
                                         @Param("typeId") Long typeId,
                                         Pageable pageable);

    /**
     * 学院/学校管理员查询待审批的申请
     */
    @Query("SELECT a FROM TutorApplication a WHERE " +
            "(:status IS NULL OR a.status = :status) " +
            "AND (:typeId IS NULL OR a.typeId = :typeId) " +
            "AND (:urgency IS NULL OR a.urgency = :urgency) " +
            "AND a.isDeleted = 0 ORDER BY " +
            "CASE WHEN a.urgency = 3 THEN 0 WHEN a.urgency = 2 THEN 1 ELSE 2 END, " +
            "a.createTime DESC")
    Page<TutorApplication> findForReview(@Param("status") Integer status,
                                         @Param("typeId") Long typeId,
                                         @Param("urgency") Integer urgency,
                                         Pageable pageable);

    /**
     * 按学院筛选（通过tutor_id关联到辅导员所管班级的学院）
     */
    @Query(value = "SELECT DISTINCT a.* FROM gc_tutor_application a " +
            "INNER JOIN gc_user u ON a.tutor_id = u.id " +
            "INNER JOIN gc_user_role ur ON u.id = ur.user_id " +
            "INNER JOIN gc_role r ON ur.role_id = r.id " +
            "WHERE (:status IS NULL OR a.status = :status) " +
            "AND (:typeId IS NULL OR a.type_id = :typeId) " +
            "AND (:urgency IS NULL OR a.urgency = :urgency) " +
            "AND a.is_deleted = 0 " +
            "ORDER BY CASE WHEN a.urgency = 3 THEN 0 WHEN a.urgency = 2 THEN 1 ELSE 2 END, a.create_time DESC",
            countQuery = "SELECT COUNT(DISTINCT a.id) FROM gc_tutor_application a " +
                    "INNER JOIN gc_user u ON a.tutor_id = u.id " +
                    "WHERE (:status IS NULL OR a.status = :status) " +
                    "AND (:typeId IS NULL OR a.type_id = :typeId) " +
                    "AND (:urgency IS NULL OR a.urgency = :urgency) " +
                    "AND a.is_deleted = 0",
            nativeQuery = true)
    Page<TutorApplication> findForReviewNative(@Param("status") Integer status,
                                                @Param("typeId") Long typeId,
                                                @Param("urgency") Integer urgency,
                                                Pageable pageable);

    /**
     * 统计各状态数量
     */
    @Query("SELECT a.status, COUNT(a) FROM TutorApplication a WHERE a.isDeleted = 0 GROUP BY a.status")
    List<Object[]> countByStatus();

    /**
     * 按辅导员ID统计
     */
    @Query("SELECT COUNT(a) FROM TutorApplication a WHERE a.tutorId = :tutorId AND a.isDeleted = 0")
    long countByTutorId(@Param("tutorId") Long tutorId);
}
