package edu.greenchannel.subsidy.repository;

import edu.greenchannel.subsidy.entity.SubsidyAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface SubsidyAllocationRepository extends JpaRepository<SubsidyAllocation, Long> {

    Optional<SubsidyAllocation> findByBatchIdAndTargetTypeAndTargetId(Long batchId, Integer targetType, Long targetId);

    @Query("SELECT COALESCE(SUM(a.amount), 0) FROM SubsidyAllocation a WHERE a.batchId = :batchId AND a.targetType = 1 AND a.targetId = :collegeId")
    BigDecimal sumAmountByBatchAndCollege(@Param("batchId") Long batchId, @Param("collegeId") Long collegeId);

    @Query("SELECT COALESCE(SUM(a.amount), 0) FROM SubsidyAllocation a WHERE a.batchId = :batchId AND a.targetType = 2 AND a.sourceId = :collegeId")
    BigDecimal sumAllocatedByCollegeToGrades(@Param("batchId") Long batchId, @Param("collegeId") Long collegeId);

    @Query("SELECT COALESCE(SUM(a.amount), 0) FROM SubsidyAllocation a WHERE a.batchId = :batchId AND a.targetType = 1")
    BigDecimal sumAllCollegesAllocationByBatch(@Param("batchId") Long batchId);

    List<SubsidyAllocation> findByBatchIdAndTargetType(Long batchId, Integer targetType);

    List<SubsidyAllocation> findByBatchId(Long batchId);
}
