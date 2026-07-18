package edu.greenchannel.subsidy;

import edu.greenchannel.common.PageResult;

import java.util.List;
import java.util.Optional;

public interface SubsidyApplyRepository {

    /** Generate a unique application number (SBDY + date + sequence). */
    String generateApplyNo();

    /** Insert a new application, returning the record with generated ID. */
    SubsidyApplyRecord insert(SubsidyApplyRecord record);

    /** Update an existing application. Returns the updated record. */
    SubsidyApplyRecord update(SubsidyApplyRecord record);

    /** Find an application by its primary key (non-deleted). */
    Optional<SubsidyApplyRecord> findById(long id);

    /** Check whether an active (non-deleted) application already exists for this batch + student. */
    boolean existsActiveByBatchAndStudent(long batchId, long studentId);

    /** Look up gc_student.id for a given gc_user.id. */
    Optional<Long> findStudentIdByUserId(long userId);

    /** Look up gc_user.college_id for a given user id. */
    Optional<Long> findCollegeIdByUserId(long userId);

    /** Search students by keyword (name or student_no) within a given college. */
    List<StudentBrief> searchStudentsInCollege(long collegeId, String keyword, int limit);

    /** Get the batch status and time window. */
    Optional<BatchInfo> getBatchInfo(long batchId);

    /** Get student info (collegeId, enrollYear) for a student record. */
    Optional<StudentInfo> getStudentInfo(long studentId);

    /** Find the grade-level allocation for a specific batch/college/grade. */
    Optional<AllocationInfo> findGradeAllocation(long batchId, long collegeId, int grade);

    /** Find the college-level allocation for a specific batch/college. */
    Optional<AllocationInfo> findCollegeAllocation(long batchId, long collegeId);

    /** Atomically increment grade-level used_amount (with overflow check). Returns true if updated. */
    boolean incrementGradeAllocation(long allocationId, java.math.BigDecimal amount);

    /** Atomically increment college-level used_amount (with overflow check). Returns true if updated. */
    boolean incrementCollegeAllocation(long allocationId, java.math.BigDecimal amount);

    /** Insert a review record. Returns the record with generated ID. */
    SubsidyReviewRecord insertReview(SubsidyReviewRecord record);

    /** Query all reviews for an application, ordered by review_time ASC. */
    List<SubsidyReviewView> findReviewsByApplyId(long applyId);

    /** Update the status and (optionally) approved amount of an application. */
    void updateApplyStatus(long applyId, int status, java.math.BigDecimal approvedAmount);

    // ---- role-filtered search ----

    /**
     * Student: only their own applications.
     */
    PageResult<SubsidyApplyView> searchForStudent(long studentUserId, Long batchId, Integer status, int page, int size);

    /**
     * Tutor / College admin: applications from students in the same college.
     */
    PageResult<SubsidyApplyView> searchForCollege(long collegeId, Long batchId, Integer status, String studentName, int page, int size);

    /**
     * School admin: all applications.
     */
    PageResult<SubsidyApplyView> searchForSchool(Long batchId, Integer status, String studentName, Long collegeId, int page, int size);

    /** Find all currently active (status=1) batches. */
    List<BatchInfo> findAvailableBatches();

    /** Build a detail view (with review timeline) for a single application. */
    Optional<SubsidyApplyView> findDetailById(long id);

    // ---- nested DTOs (used as return types from repository) ----

    record StudentBrief(long studentId, String studentNo, String name, Long collegeId, String collegeName, Integer grade) {
    }

    record BatchInfo(long id, String batchName, int status, java.time.LocalDateTime applyStartTime,
                     java.time.LocalDateTime applyEndTime, java.time.LocalDateTime collegeSubmitEndTime) {
    }

    record StudentInfo(long studentId, long collegeId, int enrollYear, java.math.BigDecimal collegeAvailableAmount) {
    }

    record AllocationInfo(long id, java.math.BigDecimal allocatedAmount, java.math.BigDecimal usedAmount) {
    }
}
