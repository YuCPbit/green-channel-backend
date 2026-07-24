package edu.greenchannel.tutor.repository;

import edu.greenchannel.tutor.entity.TutorAppStudent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 申请关联学生 Repository
 */
public interface TutorAppStudentRepository extends JpaRepository<TutorAppStudent, Long> {

    List<TutorAppStudent> findByApplicationIdAndIsDeleted(Long applicationId, Integer isDeleted);

    @Modifying
    @Query("UPDATE TutorAppStudent s SET s.isDeleted = 1 WHERE s.applicationId = :applicationId")
    void softDeleteByApplicationId(@Param("applicationId") Long applicationId);

    @Query("SELECT s FROM TutorAppStudent s WHERE s.studentId = :studentId AND s.isDeleted = 0")
    List<TutorAppStudent> findByStudentId(@Param("studentId") Long studentId);
}
