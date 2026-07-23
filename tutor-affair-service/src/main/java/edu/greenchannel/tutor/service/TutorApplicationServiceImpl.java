package edu.greenchannel.tutor.service;

import edu.greenchannel.auth.CurrentUser;
import edu.greenchannel.common.BusinessException;
import edu.greenchannel.common.PageResult;
import edu.greenchannel.tutor.dto.request.TutorApplyRequest;
import edu.greenchannel.tutor.dto.request.TutorReviewRequest;
import edu.greenchannel.tutor.dto.response.ApplyTypeResponse;
import edu.greenchannel.tutor.dto.response.StudentBrief;
import edu.greenchannel.tutor.dto.response.TutorApplyView;
import edu.greenchannel.tutor.dto.response.TutorReviewView;
import edu.greenchannel.tutor.entity.*;
import edu.greenchannel.tutor.enums.TutorAppStatus;
import edu.greenchannel.tutor.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 辅导员事务申请服务实现
 */
@Service
public class TutorApplicationServiceImpl implements TutorApplicationService {

    private final TutorApplyTypeRepository typeRepository;
    private final TutorApplicationRepository applicationRepository;
    private final TutorAppStudentRepository studentRelRepository;
    private final TutorAppReviewRepository reviewRepository;
    private final JdbcTemplate jdbcTemplate;

    public TutorApplicationServiceImpl(TutorApplyTypeRepository typeRepository,
                                       TutorApplicationRepository applicationRepository,
                                       TutorAppStudentRepository studentRelRepository,
                                       TutorAppReviewRepository reviewRepository,
                                       JdbcTemplate jdbcTemplate) {
        this.typeRepository = typeRepository;
        this.applicationRepository = applicationRepository;
        this.studentRelRepository = studentRelRepository;
        this.reviewRepository = reviewRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<ApplyTypeResponse> getApplyTypes() {
        return typeRepository.findAllActive().stream()
                .map(this::toApplyTypeResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TutorApplyView createApplication(CurrentUser user, TutorApplyRequest request) {
        TutorApplyType type = typeRepository.findById(request.typeId())
                .orElseThrow(() -> new BusinessException(40000, "申请类型不存在"));

        // 校验金额
        if (type.getNeedAmount() == 1 && request.amount() != null) {
            if (request.amount().compareTo(BigDecimal.ZERO) < 0) {
                throw new BusinessException(40000, "申请金额不能为负数");
            }
            // 从系统参数读取单笔上限
            String maxAmountStr = getConfigValue("TUTOR_APP_MAX_AMOUNT");
            if (maxAmountStr != null) {
                BigDecimal maxAmount = new BigDecimal(maxAmountStr);
                if (request.amount().compareTo(maxAmount) > 0) {
                    throw new BusinessException(40000, "申请金额不能超过单笔上限" + maxAmount + "元");
                }
            }
        }

        // 校验关联学生
        if (type.getNeedStudent() == 1 && (request.studentIds() == null || request.studentIds().isEmpty())) {
            throw new BusinessException(40000, "该申请类型需要关联至少一个学生");
        }

        TutorApplication app = new TutorApplication();
        app.setApplyNo(generateApplyNo());
        app.setTypeId(request.typeId());
        app.setTutorId(user.id());
        app.setTitle(request.title());
        app.setDescription(request.description());
        app.setAmount(request.amount());
        app.setUrgency(request.urgency() != null ? request.urgency() : 1);
        app.setFormData(request.formData());
        app.setStatus(TutorAppStatus.DRAFT.getCode());

        TutorApplication saved = applicationRepository.save(app);

        // 保存关联学生
        if (request.studentIds() != null) {
            saveStudentRelations(saved.getId(), request.studentIds());
        }

        return buildApplyView(saved);
    }

    @Override
    @Transactional
    public TutorApplyView updateApplication(CurrentUser user, Long id, TutorApplyRequest request) {
        TutorApplication app = findOwnApplication(user, id);

        if (app.getStatus() != TutorAppStatus.DRAFT.getCode()
                && app.getStatus() != TutorAppStatus.REJECTED.getCode()) {
            throw new BusinessException(40900, "仅草稿或已驳回状态的申请可修改");
        }

        TutorApplyType type = typeRepository.findById(request.typeId())
                .orElseThrow(() -> new BusinessException(40000, "申请类型不存在"));

        app.setTypeId(request.typeId());
        app.setTitle(request.title());
        app.setDescription(request.description());
        app.setAmount(request.amount());
        app.setUrgency(request.urgency() != null ? request.urgency() : 1);
        app.setFormData(request.formData());

        // 更新关联学生
        if (request.studentIds() != null) {
            studentRelRepository.softDeleteByApplicationId(id);
            saveStudentRelations(id, request.studentIds());
        }

        TutorApplication saved = applicationRepository.save(app);
        return buildApplyView(saved);
    }

    @Override
    @Transactional
    public TutorApplyView submitDraft(CurrentUser user, Long id) {
        TutorApplication app = findOwnApplication(user, id);

        if (app.getStatus() != TutorAppStatus.DRAFT.getCode()
                && app.getStatus() != TutorAppStatus.REJECTED.getCode()) {
            throw new BusinessException(40900, "仅草稿或已驳回状态的申请可提交");
        }

        app.setStatus(TutorAppStatus.PENDING_COLLEGE.getCode());
        app.setSubmitTime(LocalDateTime.now());
        TutorApplication saved = applicationRepository.save(app);
        return buildApplyView(saved);
    }

    @Override
    public PageResult<TutorApplyView> listMyApplications(CurrentUser user, Integer status, Long typeId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createTime").descending());
        Page<TutorApplication> pageResult = applicationRepository.findByTutorId(user.id(), status, typeId, pageable);

        List<TutorApplyView> views = pageResult.getContent().stream()
                .map(this::buildApplyView)
                .collect(Collectors.toList());

        return new PageResult<>(views, pageResult.getTotalElements(), page, size);
    }

    @Override
    public PageResult<TutorApplyView> listPendingReviews(CurrentUser user, Integer status, Long typeId, Integer urgency, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.unsorted());

        // 根据用户角色决定筛选的状态
        Integer filterStatus = status;
        if (filterStatus == null) {
            // 学院管理员默认看待学院审批的，学校管理员默认看待学校审批的
            if (user.userType() == 3) { // 学院管理员
                filterStatus = TutorAppStatus.PENDING_COLLEGE.getCode();
            } else if (user.userType() == 4) { // 学校管理员
                filterStatus = TutorAppStatus.PENDING_SCHOOL.getCode();
            }
        }

        Page<TutorApplication> pageResult = applicationRepository.findForReviewNative(filterStatus, typeId, urgency, pageable);

        List<TutorApplyView> views = pageResult.getContent().stream()
                .map(this::buildApplyView)
                .collect(Collectors.toList());

        return new PageResult<>(views, pageResult.getTotalElements(), page, size);
    }

    @Override
    public TutorApplyView getDetail(CurrentUser user, Long id) {
        TutorApplication app = applicationRepository.findById(id)
                .orElseThrow(() -> new BusinessException(40400, "申请不存在"));
        return buildApplyView(app);
    }

    @Override
    @Transactional
    public void submitReview(CurrentUser user, TutorReviewRequest request) {
        TutorApplication app = applicationRepository.findById(request.applicationId())
                .orElseThrow(() -> new BusinessException(40400, "申请不存在"));

        // 获取申请类型配置
        TutorApplyType type = typeRepository.findById(app.getTypeId())
                .orElseThrow(() -> new BusinessException(40000, "申请类型配置不存在"));

        int reviewerRole;
        int currentStatus = app.getStatus();

        // 根据当前状态确定审核角色
        if (currentStatus == TutorAppStatus.PENDING_COLLEGE.getCode()) {
            reviewerRole = 2; // 学院
        } else if (currentStatus == TutorAppStatus.PENDING_SCHOOL.getCode()) {
            reviewerRole = 3; // 学校
        } else {
            throw new BusinessException(40900, "当前申请状态不允许审核操作");
        }

        // 保存审核记录
        TutorAppReview review = new TutorAppReview();
        review.setApplicationId(app.getId());
        review.setReviewerId(user.id());
        review.setReviewerRole(reviewerRole);
        review.setAction(request.action());
        review.setComment(request.comment());
        reviewRepository.save(review);

        // 更新申请状态
        // 审核动作: 1-通过 2-驳回 3-转交 4-备案
        if (request.action() == 1) { // 通过
            if (reviewerRole == 2) { // 学院通过
                if (type.getApprovalLevel() == 1) {
                    // 仅需学院审批，直接通过
                    app.setStatus(TutorAppStatus.APPROVED.getCode());
                } else {
                    // 需要学校审批，流转到待学校审批
                    app.setStatus(TutorAppStatus.PENDING_SCHOOL.getCode());
                }
            } else if (reviewerRole == 3) { // 学校通过
                app.setStatus(TutorAppStatus.APPROVED.getCode());
            }
        } else if (request.action() == 2) { // 驳回
            app.setStatus(TutorAppStatus.REJECTED.getCode());
        } else if (request.action() == 3) { // 转交（仅记录，状态不变）
            // 转交不改变状态，仅记录审核意见
        } else if (request.action() == 4) { // 备案（学校终审备案）
            if (reviewerRole == 3) {
                app.setStatus(TutorAppStatus.APPROVED.getCode());
            }
        }

        // 审批通过后，若涉及金额则标记为待下发
        if (app.getStatus() == TutorAppStatus.APPROVED.getCode() && app.getAmount() != null && app.getAmount().compareTo(BigDecimal.ZERO) > 0) {
            app.setDisburseStatus(1); // 待下发
        }

        applicationRepository.save(app);
    }

    // ==================== 资金下发 ====================

    @Override
    @Transactional
    public void disburse(CurrentUser user, Long id) {
        TutorApplication app = applicationRepository.findById(id)
                .orElseThrow(() -> new BusinessException(40400, "申请不存在"));
        validateCanDisburse(app);
        doDisburse(app, user.id());
    }

    @Override
    @Transactional
    public int batchDisburse(CurrentUser user, List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(40000, "请选择要下发的申请");
        }
        int count = 0;
        for (Long id : ids) {
            TutorApplication app = applicationRepository.findById(id)
                    .orElse(null);
            if (app == null) continue;
            try {
                validateCanDisburse(app);
                doDisburse(app, user.id());
                count++;
            } catch (BusinessException ignored) {
                // 跳过不符合的，继续处理
            }
        }
        if (count == 0) {
            throw new BusinessException(40000, "所选申请均不符合下发条件");
        }
        return count;
    }

    @Override
    public PageResult<TutorApplyView> listDisburse(CurrentUser user, Integer disburseStatus, Long typeId, int page, int size) {
        // 原生查询自带 ORDER BY，Sort 必须为 unsorted，否则会与原生 SQL 冲突导致 500
        Pageable pageable = PageRequest.of(page - 1, size, Sort.unsorted());
        Integer approvedStatus = TutorAppStatus.APPROVED.getCode();
        Page<TutorApplication> pageResult = applicationRepository.findForReviewNative(approvedStatus, typeId, null, pageable);

        List<TutorApplyView> all = pageResult.getContent().stream()
                .map(this::buildApplyView)
                .collect(Collectors.toList());

        List<TutorApplyView> filtered;
        if (disburseStatus != null) {
            filtered = all.stream()
                    .filter(v -> v.disburseStatus() != null && v.disburseStatus().equals(disburseStatus))
                    .collect(Collectors.toList());
        } else {
            filtered = all.stream()
                    .filter(v -> v.disburseStatus() != null && v.disburseStatus() > 0)
                    .collect(Collectors.toList());
        }

        return new PageResult<>(filtered, filtered.size(), page, size);
    }

    @Override
    public Map<String, Object> getDisburseSummary(CurrentUser user) {
        Map<String, Object> summary = new LinkedHashMap<>();
        try {
            Long pendingCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM gc_tutor_application WHERE status = ? AND disburse_status = 1 AND is_deleted = 0",
                    Long.class, TutorAppStatus.APPROVED.getCode());
            Long disbursedCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM gc_tutor_application WHERE status = ? AND disburse_status = 2 AND is_deleted = 0",
                    Long.class, TutorAppStatus.APPROVED.getCode());
            BigDecimal pendingAmount = jdbcTemplate.queryForObject(
                    "SELECT COALESCE(SUM(amount), 0) FROM gc_tutor_application WHERE status = ? AND disburse_status = 1 AND is_deleted = 0",
                    BigDecimal.class, TutorAppStatus.APPROVED.getCode());
            BigDecimal disbursedAmount = jdbcTemplate.queryForObject(
                    "SELECT COALESCE(SUM(amount), 0) FROM gc_tutor_application WHERE status = ? AND disburse_status = 2 AND is_deleted = 0",
                    BigDecimal.class, TutorAppStatus.APPROVED.getCode());

            summary.put("pendingCount", pendingCount != null ? pendingCount : 0L);
            summary.put("disbursedCount", disbursedCount != null ? disbursedCount : 0L);
            summary.put("pendingAmount", pendingAmount != null ? pendingAmount : BigDecimal.ZERO);
            summary.put("disbursedAmount", disbursedAmount != null ? disbursedAmount : BigDecimal.ZERO);
        } catch (Exception e) {
            summary.put("pendingCount", 0L); summary.put("disbursedCount", 0L);
            summary.put("pendingAmount", BigDecimal.ZERO); summary.put("disbursedAmount", BigDecimal.ZERO);
        }
        return summary;
    }

    private void validateCanDisburse(TutorApplication app) {
        if (app.getStatus() != TutorAppStatus.APPROVED.getCode()) {
            throw new BusinessException(40900, "仅已通过的申请可下发资金");
        }
        if (app.getAmount() == null || app.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(40900, "该申请不涉及资金下发");
        }
        if (app.getDisburseStatus() != null && app.getDisburseStatus() == 2) {
            throw new BusinessException(40900, "该申请资金已下发，请勿重复操作");
        }
    }

    private void doDisburse(TutorApplication app, Long operatorId) {
        app.setDisburseStatus(2);
        app.setDisburseTime(LocalDateTime.now());
        app.setDisburseOperatorId(operatorId);
        applicationRepository.save(app);
    }

    @Override
    public List<StudentBrief> searchStudents(CurrentUser user, String keyword) {
        // 查询辅导员所管班级的学生
        String sql = """
                SELECT s.id AS studentId, s.name AS studentName, s.student_no AS studentNo,
                       s.college_id AS collegeId, col.college_name AS collegeName,
                       c.class_name AS className, s.poverty_level AS povertyLevel
                FROM gc_student s
                INNER JOIN gc_class c ON s.class_id = c.id
                INNER JOIN gc_college col ON s.college_id = col.id
                WHERE c.tutor_id = ? AND s.is_deleted = 0
                  AND (s.name LIKE ? OR s.student_no LIKE ?)
                LIMIT 50""";

        String likeKeyword = "%" + keyword + "%";
        return jdbcTemplate.query(sql,
                (rs, rowNum) -> new StudentBrief(
                        rs.getLong("studentId"),
                        rs.getString("studentName"),
                        rs.getString("studentNo"),
                        rs.getLong("collegeId"),
                        rs.getString("collegeName"),
                        rs.getString("className"),
                        rs.getObject("povertyLevel") != null ? rs.getInt("povertyLevel") : null
                ),
                user.id(), likeKeyword, likeKeyword);
    }

    @Override
    public Map<String, Object> getStatistics(CurrentUser user) {
        Map<String, Object> stats = new LinkedHashMap<>();

        // 统计各状态数量
        List<Object[]> statusCounts = applicationRepository.countByStatus();
        long total = 0;
        Map<Integer, Long> countMap = new LinkedHashMap<>();
        for (Object[] row : statusCounts) {
            int status = ((Number) row[0]).intValue();
            long count = ((Number) row[1]).longValue();
            countMap.put(status, count);
            total += count;
        }
        stats.put("total", total);
        stats.put("draft", countMap.getOrDefault(TutorAppStatus.DRAFT.getCode(), 0L));
        stats.put("pendingCollege", countMap.getOrDefault(TutorAppStatus.PENDING_COLLEGE.getCode(), 0L));
        stats.put("pendingSchool", countMap.getOrDefault(TutorAppStatus.PENDING_SCHOOL.getCode(), 0L));
        stats.put("approved", countMap.getOrDefault(TutorAppStatus.APPROVED.getCode(), 0L));
        stats.put("rejected", countMap.getOrDefault(TutorAppStatus.REJECTED.getCode(), 0L));

        // 资金下发统计
        try {
            Long pendingDisburse = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM gc_tutor_application WHERE status = ? AND disburse_status = 1 AND is_deleted = 0",
                    Long.class, TutorAppStatus.APPROVED.getCode());
            Long disbursed = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM gc_tutor_application WHERE status = ? AND disburse_status = 2 AND is_deleted = 0",
                    Long.class, TutorAppStatus.APPROVED.getCode());
            stats.put("pendingDisburse", pendingDisburse != null ? pendingDisburse : 0L);
            stats.put("disbursed", disbursed != null ? disbursed : 0L);
        } catch (Exception ignored) {
            stats.put("pendingDisburse", 0L);
            stats.put("disbursed", 0L);
        }

        return stats;
    }

    // ==================== 私有方法 ====================

    private TutorApplication findOwnApplication(CurrentUser user, Long id) {
        TutorApplication app = applicationRepository.findById(id)
                .orElseThrow(() -> new BusinessException(40400, "申请不存在"));
        if (!app.getTutorId().equals(user.id())) {
            throw new BusinessException(40300, "无权操作此申请");
        }
        return app;
    }

    private void saveStudentRelations(Long applicationId, List<Long> studentIds) {
        for (Long studentId : studentIds) {
            TutorAppStudent rel = new TutorAppStudent();
            rel.setApplicationId(applicationId);
            rel.setStudentId(studentId);
            studentRelRepository.save(rel);
        }
    }

    private String generateApplyNo() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String randomPart = String.format("%04d", new Random().nextInt(10000));
        return "TA" + datePart + randomPart;
    }

    private TutorApplyView buildApplyView(TutorApplication app) {
        // 获取申请类型名称
        String typeName = typeRepository.findById(app.getTypeId())
                .map(TutorApplyType::getTypeName)
                .orElse("未知类型");

        // 获取辅导员姓名
        String tutorName = queryUserName(app.getTutorId());

        // 获取审核记录
        List<TutorReviewView> reviews = reviewRepository
                .findByApplicationIdAndIsDeletedOrderByReviewTimeAsc(app.getId(), 0)
                .stream()
                .map(r -> new TutorReviewView(
                        r.getId(),
                        r.getReviewerId(),
                        queryUserName(r.getReviewerId()),
                        r.getReviewerRole(),
                        r.getAction(),
                        r.getComment(),
                        r.getReviewTime()))
                .collect(Collectors.toList());

        // 获取关联学生
        List<StudentBrief> students = studentRelRepository
                .findByApplicationIdAndIsDeleted(app.getId(), 0)
                .stream()
                .map(rel -> queryStudentBrief(rel.getStudentId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return new TutorApplyView(
                app.getId(),
                app.getApplyNo(),
                app.getTypeId(),
                typeName,
                app.getTutorId(),
                tutorName,
                app.getTitle(),
                app.getDescription(),
                app.getAmount(),
                app.getUrgency(),
                app.getStatus(),
                app.getFormData(),
                app.getRemark(),
                app.getApplyTime(),
                app.getSubmitTime(),
                app.getDisburseStatus(),
                app.getDisburseTime(),
                reviews,
                students);
    }

    private String queryUserName(Long userId) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT real_name FROM gc_user WHERE id = ?",
                    String.class, userId);
        } catch (Exception e) {
            return "未知用户";
        }
    }

    private StudentBrief queryStudentBrief(Long studentId) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT s.id AS studentId, s.name AS studentName, s.student_no AS studentNo, " +
                            "s.college_id AS collegeId, col.college_name AS collegeName, " +
                            "c.class_name AS className, s.poverty_level AS povertyLevel " +
                            "FROM gc_student s " +
                            "LEFT JOIN gc_college col ON s.college_id = col.id " +
                            "LEFT JOIN gc_class c ON s.class_id = c.id " +
                            "WHERE s.id = ? AND s.is_deleted = 0",
                    (rs, rowNum) -> new StudentBrief(
                            rs.getLong("studentId"),
                            rs.getString("studentName"),
                            rs.getString("studentNo"),
                            rs.getLong("collegeId"),
                            rs.getString("collegeName"),
                            rs.getString("className"),
                            rs.getObject("povertyLevel") != null ? rs.getInt("povertyLevel") : null
                    ),
                    studentId);
        } catch (Exception e) {
            return null;
        }
    }

    private String getConfigValue(String configKey) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT config_value FROM gc_system_config WHERE config_key = ?",
                    String.class, configKey);
        } catch (Exception e) {
            return null;
        }
    }

    private ApplyTypeResponse toApplyTypeResponse(TutorApplyType entity) {
        return new ApplyTypeResponse(
                entity.getId(),
                entity.getTypeName(),
                entity.getTypeCode(),
                entity.getDescription(),
                entity.getNeedAmount(),
                entity.getNeedStudent(),
                entity.getApprovalLevel(),
                entity.getFormTemplate(),
                entity.getSort(),
                entity.getStatus());
    }
}
