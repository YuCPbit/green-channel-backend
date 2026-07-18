package edu.greenchannel.subsidy.repository;

import edu.greenchannel.subsidy.entity.SubsidyBatch;
import edu.greenchannel.subsidy.enums.BatchStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SubsidyBatchRepository extends JpaRepository<SubsidyBatch, Long> {

    @Query("SELECT b FROM SubsidyBatch b WHERE " +
            "(:batchName IS NULL OR b.batchName LIKE %:batchName%) " +
            "AND (:status IS NULL OR b.status = :status)")
    Page<SubsidyBatch> findByCondition(@Param("batchName") String batchName,
                                       @Param("status") BatchStatus status,
                                       Pageable pageable);

    @Query("SELECT COUNT(b) FROM SubsidyBatch b WHERE b.academicYear = :academicYear AND b.subsidyType = :subsidyType AND (:excludeId IS NULL OR b.id <> :excludeId)")
    long countByAcademicYearAndSubsidyType(@Param("academicYear") String academicYear,
                                           @Param("subsidyType") Integer subsidyType,
                                           @Param("excludeId") Long excludeId);
}
