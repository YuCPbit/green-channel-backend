package edu.greenchannel.subsidy.service;

import edu.greenchannel.auth.CurrentUser;
import edu.greenchannel.common.BusinessException;
import edu.greenchannel.common.PageResult;
import edu.greenchannel.subsidy.entity.SubsidyApplyRecord;
import edu.greenchannel.subsidy.dto.response.SubsidyApplyView;
import edu.greenchannel.subsidy.entity.SubsidyReviewRecord;
import edu.greenchannel.subsidy.dto.response.SubsidyReviewView;
import edu.greenchannel.subsidy.dto.request.SubsidyApplyRequest;
import edu.greenchannel.subsidy.dto.request.SubsidyReviewRequest;
import edu.greenchannel.subsidy.repository.SubsidyApplyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class SubsidyApplyService {

    private final SubsidyApplyRepository repository;

    public SubsidyApplyService(SubsidyApplyRepository repository) {
        this.repository = repository;
    }

    // ---------------------------------------------------------------
    // Student self-apply
    // ---------------------------------------------------------------

    public SubsidyApplyView submitStudentApply(CurrentUser user, SubsidyApplyRequest request) {
        if (request.batchId() == null) {
            throw new BusinessException(40001, "请选择补助批次");
        }
        if (request.applyAmount() == null || request.applyAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(40001, "申请金额必须大于0");
        }
        if (request.applyReason() == null || request.applyReason().isBlank()) {
            throw new BusinessException(40001, "请填写申请理由");
        }
        if (user.userType() != 1) {
            throw new BusinessException(40300, "仅学生可以提交申请");
        }

        var batchInfo = repository.getBatchInfo(request.batchId())
                .orElseThrow(() -> new BusinessException(40400, "补助批次不存在"));

        if (batchInfo.status() != 1) {
            throw new BusinessException(40900, "当前批次不在申请开放期");
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(batchInfo.applyStartTime()) || now.isAfter(batchInfo.applyEndTime())) {
            throw new BusinessException(40900, "当前不在申请时间窗口内");
        }

        long studentId = repository.findStudentIdByUserId(user.id())
                .orElseThrow(() -> new BusinessException(40400, "未找到对应的学生档案"));

        if (repository.existsActiveByBatchAndStudent(request.batchId(), studentId)) {
            throw new BusinessException(40900, "您在此批次中已有有效申请，不可重复提交");
        }

        String applyNo = repository.generateApplyNo();
        SubsidyApplyRecord record = new SubsidyApplyRecord(
                0, request.batchId(), studentId,
                SubsidyApplyRecord.APPLICANT_TYPE_STUDENT, user.id(), applyNo,
                0, request.applyAmount(), null, request.applyReason(),
                SubsidyApplyRecord.STATUS_PENDING_TUTOR, null);

        SubsidyApplyRecord saved = repository.insert(record);
        return repository.findDetailById(saved.id())
                .orElseThrow(() -> new BusinessException(50000, "申请创建失败"));
    }

    // ---------------------------------------------------------------
    // Tutor proxy apply
    // ---------------------------------------------------------------

    public SubsidyApplyView submitTutorApply(CurrentUser user, SubsidyApplyRequest request) {
        if (request.batchId() == null || request.studentId() == null) {
            throw new BusinessException(40001, "请选择补助批次和学生");
        }
        if (request.applyAmount() == null || request.applyAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(40001, "申请金额必须大于0");
        }
        if (request.applyReason() == null || request.applyReason().isBlank()) {
            throw new BusinessException(40001, "请填写申请理由");
        }
        if (user.userType() != 2) {
            throw new BusinessException(40300, "仅辅导员可代学生申请");
        }

        var batchInfo = repository.getBatchInfo(request.batchId())
                .orElseThrow(() -> new BusinessException(40400, "补助批次不存在"));
        if (batchInfo.status() != 1) {
            throw new BusinessException(40900, "当前批次不在申请开放期");
        }

        long tutorCollegeId = repository.findCollegeIdByUserId(user.id())
                .orElseThrow(() -> new BusinessException(40400, "未找到辅导员的学院归属"));

        var studentInfo = repository.getStudentInfo(request.studentId())
                .orElseThrow(() -> new BusinessException(40400, "学生档案不存在"));
        if (studentInfo.collegeId() != tutorCollegeId) {
            throw new BusinessException(40300, "只能为本人所管学院的学生发起申请");
        }

        if (repository.existsActiveByBatchAndStudent(request.batchId(), request.studentId())) {
            throw new BusinessException(40900, "该学生在此批次中已有有效申请");
        }

        String applyNo = repository.generateApplyNo();
        SubsidyApplyRecord record = new SubsidyApplyRecord(
                0, request.batchId(), request.studentId(),
                SubsidyApplyRecord.APPLICANT_TYPE_TUTOR, user.id(), applyNo,
                0, request.applyAmount(), null, request.applyReason(),
                SubsidyApplyRecord.STATUS_PENDING_COLLEGE, null);

        SubsidyApplyRecord saved = repository.insert(record);
        return repository.findDetailById(saved.id())
                .orElseThrow(() -> new BusinessException(50000, "申请创建失败"));
    }

    // ---------------------------------------------------------------
    // Resubmit after return
    // ---------------------------------------------------------------

    public SubsidyApplyView resubmitApply(CurrentUser user, long applyId, SubsidyApplyRequest request) {
        var apply = repository.findById(applyId)
                .orElseThrow(() -> new BusinessException(40400, "申请不存在"));
        if (apply.status() != SubsidyApplyRecord.STATUS_RETURNED) {
            throw new BusinessException(40900, "只有被退回的申请才可以重新提交");
        }

        long studentId = repository.findStudentIdByUserId(user.id())
                .orElseThrow(() -> new BusinessException(40400, "未找到学生档案"));
        if (apply.studentId() != studentId) {
            throw new BusinessException(40300, "只能修改本人的申请");
        }

        if (request.applyAmount() == null || request.applyAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(40001, "申请金额必须大于0");
        }

        // Determine the pending status from the last review
        List<SubsidyReviewView> reviews = repository.findReviewsByApplyId(applyId);
        int newStatus = apply.applicantType() == SubsidyApplyRecord.APPLICANT_TYPE_TUTOR
                ? SubsidyApplyRecord.STATUS_PENDING_COLLEGE
                : SubsidyApplyRecord.STATUS_PENDING_TUTOR;

        SubsidyApplyRecord updated = new SubsidyApplyRecord(
                apply.id(), apply.batchId(), apply.studentId(), apply.applicantType(),
                apply.applicantUserId(), apply.applyNo(), apply.subsidyType(),
                request.applyAmount(), apply.approvedAmount(), request.applyReason(),
                newStatus, apply.applyTime());
        repository.update(updated);
        return repository.findDetailById(applyId)
                .orElseThrow(() -> new BusinessException(50000, "重新提交失败"));
    }

    // ---------------------------------------------------------------
    // Submit review
    // ---------------------------------------------------------------

    public void submitReview(CurrentUser user, SubsidyReviewRequest request) {
        var apply = repository.findById(request.applyId())
                .orElseThrow(() -> new BusinessException(40400, "申请不存在"));

        int reviewerRole = determineReviewerRole(user.userType());
        if (reviewerRole == 0) {
            throw new BusinessException(40300, "无权进行审核操作");
        }

        validateReviewerAccess(apply, reviewerRole, user);

        if (request.action() == SubsidyReviewRecord.ACTION_RETURN || request.action() == SubsidyReviewRecord.ACTION_REJECT) {
            if (request.comment() == null || request.comment().isBlank()) {
                throw new BusinessException(40001, "驳回/不通过时审核意见为必填");
            }
        }

        if (request.action() == SubsidyReviewRecord.ACTION_PASS) {
            handlePass(apply, reviewerRole, request);
        } else if (request.action() == SubsidyReviewRecord.ACTION_RETURN) {
            repository.updateApplyStatus(apply.id(), SubsidyApplyRecord.STATUS_RETURNED, null);
        } else if (request.action() == SubsidyReviewRecord.ACTION_REJECT) {
            repository.updateApplyStatus(apply.id(), SubsidyApplyRecord.STATUS_REJECTED, null);
        } else {
            throw new BusinessException(40001, "无效的审核动作");
        }

        SubsidyReviewRecord review = new SubsidyReviewRecord(
                0, apply.id(), user.id(), reviewerRole,
                request.action(), request.comment(), request.suggestAmount(), null);
        repository.insertReview(review);
    }

    private void handlePass(SubsidyApplyRecord apply, int reviewerRole, SubsidyReviewRequest request) {
        switch (reviewerRole) {
            case SubsidyReviewRecord.ROLE_TUTOR -> {
                // Counselor pass: validate grade quota, advance to college review
                if (request.suggestAmount() == null || request.suggestAmount().compareTo(BigDecimal.ZERO) <= 0) {
                    throw new BusinessException(40001, "请填写建议补助金额");
                }
                checkGradeQuota(apply, request.suggestAmount());
                repository.updateApplyStatus(apply.id(), SubsidyApplyRecord.STATUS_PENDING_COLLEGE, request.suggestAmount());
            }
            case SubsidyReviewRecord.ROLE_COLLEGE -> {
                // College pass: validate college quota, advance to school review
                if (request.suggestAmount() == null || request.suggestAmount().compareTo(BigDecimal.ZERO) <= 0) {
                    throw new BusinessException(40001, "请填写建议补助金额");
                }
                checkCollegeQuota(apply, request.suggestAmount());
                repository.updateApplyStatus(apply.id(), SubsidyApplyRecord.STATUS_PENDING_SCHOOL, request.suggestAmount());
            }
            case SubsidyReviewRecord.ROLE_SCHOOL -> {
                // School pass: final approval — deduct both grade and college quotas atomically
                BigDecimal finalAmount = request.suggestAmount() != null
                        ? request.suggestAmount()
                        : apply.approvedAmount();
                if (finalAmount == null || finalAmount.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new BusinessException(40001, "最终发放金额必须大于0");
                }
                deductGradeQuota(apply, finalAmount);
                deductCollegeQuota(apply, finalAmount);
                repository.updateApplyStatus(apply.id(), SubsidyApplyRecord.STATUS_APPROVED, finalAmount);
            }
            default -> throw new BusinessException(40001, "无效的审核角色");
        }
    }

    // ---------------------------------------------------------------
    // List applies (role-filtered)
    // ---------------------------------------------------------------

    public PageResult<SubsidyApplyView> listApplies(CurrentUser user, Long batchId, Integer status, String studentName, int page, int size) {
        return switch (user.userType()) {
            case 1 -> {
                long studentId = repository.findStudentIdByUserId(user.id()).orElse(0L);
                yield repository.searchForStudent(user.id(), batchId, status, page, size);
            }
            case 2, 3 -> {
                long collegeId = repository.findCollegeIdByUserId(user.id()).orElseThrow(() -> new BusinessException(40400, "未找到学院归属"));
                yield repository.searchForCollege(collegeId, batchId, status, studentName, page, size);
            }
            case 4 -> repository.searchForSchool(batchId, status, studentName, null, page, size);
            default -> throw new BusinessException(40300, "无权查看申请列表");
        };
    }

    // ---------------------------------------------------------------
    // Detail
    // ---------------------------------------------------------------

    public SubsidyApplyView getApplyDetail(CurrentUser user, long id) {
        var detail = repository.findDetailById(id)
                .orElseThrow(() -> new BusinessException(40400, "申请不存在"));
        // Visibility check
        if (user.userType() == 1) {
            long studentId = repository.findStudentIdByUserId(user.id()).orElse(0L);
            if (detail.studentId() != studentId) {
                throw new BusinessException(40300, "无权查看该申请");
            }
        } else if (user.userType() == 2 || user.userType() == 3) {
            Long collegeId = repository.findCollegeIdByUserId(user.id()).orElseThrow(() -> new BusinessException(40400, "未找到学院归属"));
            if (!collegeId.equals(detail.collegeId())) {
                throw new BusinessException(40300, "无权查看其他学院的申请");
            }
        }
        // user_type 4 (school) can see all
        return detail;
    }

    // ---------------------------------------------------------------
    // Supporting queries
    // ---------------------------------------------------------------

    public List<SubsidyApplyRepository.StudentBrief> searchStudents(CurrentUser user, String keyword) {
        long collegeId = repository.findCollegeIdByUserId(user.id())
                .orElseThrow(() -> new BusinessException(40400, "未找到学院归属"));
        if (user.userType() != 2) {
            throw new BusinessException(40300, "仅辅导员可以使用学生搜索");
        }
        return repository.searchStudentsInCollege(collegeId, keyword, 20);
    }

    public List<SubsidyApplyRepository.BatchInfo> getAvailableBatches() {
        return repository.findAvailableBatches();
    }

    // ---------------------------------------------------------------
    // Internal helpers
    // ---------------------------------------------------------------

    private int determineReviewerRole(int userType) {
        return switch (userType) {
            case 2 -> SubsidyReviewRecord.ROLE_TUTOR;
            case 3 -> SubsidyReviewRecord.ROLE_COLLEGE;
            case 4 -> SubsidyReviewRecord.ROLE_SCHOOL;
            default -> 0;
        };
    }

    private void validateReviewerAccess(SubsidyApplyRecord apply, int reviewerRole, CurrentUser user) {
        // Check status matches
        int expectedStatus = switch (reviewerRole) {
            case SubsidyReviewRecord.ROLE_TUTOR -> SubsidyApplyRecord.STATUS_PENDING_TUTOR;
            case SubsidyReviewRecord.ROLE_COLLEGE -> SubsidyApplyRecord.STATUS_PENDING_COLLEGE;
            case SubsidyReviewRecord.ROLE_SCHOOL -> SubsidyApplyRecord.STATUS_PENDING_SCHOOL;
            default -> throw new BusinessException(40001, "无效审核角色");
        };
        if (apply.status() != expectedStatus) {
            throw new BusinessException(40900, "当前申请状态不允许此审核操作");
        }
        // For counselor and college: verify they belong to the same college
        if (reviewerRole == SubsidyReviewRecord.ROLE_TUTOR || reviewerRole == SubsidyReviewRecord.ROLE_COLLEGE) {
            var studentInfo = repository.getStudentInfo(apply.studentId())
                    .orElseThrow(() -> new BusinessException(40400, "学生档案不存在"));
            long userCollegeId = repository.findCollegeIdByUserId(user.id())
                    .orElseThrow(() -> new BusinessException(40400, "未找到学院归属"));
            if (studentInfo.collegeId() != userCollegeId) {
                throw new BusinessException(40300, "不能审核其他学院的申请");
            }
        }
        // Counselor: cannot review their own proxy applications
        if (reviewerRole == SubsidyReviewRecord.ROLE_TUTOR && apply.applicantType() == SubsidyApplyRecord.APPLICANT_TYPE_TUTOR) {
            throw new BusinessException(40900, "辅导员不能审核自己代学生发起的申请，该申请已直接提交至学院");
        }
    }

    private void checkGradeQuota(SubsidyApplyRecord apply, BigDecimal amount) {
        var studentInfo = repository.getStudentInfo(apply.studentId())
                .orElseThrow(() -> new BusinessException(40400, "学生档案不存在"));
        var gradeAlloc = repository.findGradeAllocation(apply.batchId(), studentInfo.collegeId(), studentInfo.enrollYear())
                .orElseThrow(() -> new BusinessException(40900, "该年级尚未分配补助额度，请联系学院管理员"));
        if (gradeAlloc.usedAmount().add(amount).compareTo(gradeAlloc.allocatedAmount()) > 0) {
            throw new BusinessException(40900, "年级补助额度不足，请联系学院管理员");
        }
    }

    private void checkCollegeQuota(SubsidyApplyRecord apply, BigDecimal amount) {
        var studentInfo = repository.getStudentInfo(apply.studentId())
                .orElseThrow(() -> new BusinessException(40400, "学生档案不存在"));
        var collegeAlloc = repository.findCollegeAllocation(apply.batchId(), studentInfo.collegeId())
                .orElseThrow(() -> new BusinessException(40900, "该学院尚未分配补助额度，请联系学校资助中心"));
        if (collegeAlloc.usedAmount().add(amount).compareTo(collegeAlloc.allocatedAmount()) > 0) {
            throw new BusinessException(40900, "学院补助额度不足，请联系学校资助中心");
        }
    }

    private void deductGradeQuota(SubsidyApplyRecord apply, BigDecimal amount) {
        var studentInfo = repository.getStudentInfo(apply.studentId())
                .orElseThrow(() -> new BusinessException(40400, "学生档案不存在"));
        var gradeAlloc = repository.findGradeAllocation(apply.batchId(), studentInfo.collegeId(), studentInfo.enrollYear())
                .orElseThrow(() -> new BusinessException(40900, "该年级尚未分配补助额度，请联系学院管理员"));
        if (!repository.incrementGradeAllocation(gradeAlloc.id(), amount)) {
            throw new BusinessException(40900, "年级补助额度不足，请联系学院管理员");
        }
    }

    private void deductCollegeQuota(SubsidyApplyRecord apply, BigDecimal amount) {
        var studentInfo = repository.getStudentInfo(apply.studentId())
                .orElseThrow(() -> new BusinessException(40400, "学生档案不存在"));
        var collegeAlloc = repository.findCollegeAllocation(apply.batchId(), studentInfo.collegeId())
                .orElseThrow(() -> new BusinessException(40900, "该学院尚未分配补助额度，请联系学校资助中心"));
        if (!repository.incrementCollegeAllocation(collegeAlloc.id(), amount)) {
            throw new BusinessException(40900, "学院补助额度不足，请联系学校资助中心");
        }
    }
}
