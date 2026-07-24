package edu.greenchannel.subsidy.service;

import edu.greenchannel.auth.CurrentUser;
import edu.greenchannel.common.BusinessException;
import edu.greenchannel.subsidy.controller.SupportController.AidPlanRequest;
import edu.greenchannel.subsidy.controller.SupportController.AppealCreateRequest;
import edu.greenchannel.subsidy.controller.SupportController.AppealHandleRequest;
import edu.greenchannel.subsidy.controller.SupportController.SurveyCreateRequest;
import edu.greenchannel.subsidy.controller.SupportController.SurveyResponseRequest;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SupportService {

    private static final int PLAN_DRAFT = 0;
    private static final int PLAN_PUBLISHED = 1;
    private static final int PLAN_OFFLINE = 2;
    private final JdbcTemplate jdbc;

    public SupportService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Map<String, Object>> listPlans(Integer status, CurrentUser user) {
        boolean student = user.userType() == 1;
        StringBuilder sql = new StringBuilder("""
                SELECT id, plan_name, fund_source, amount_mode, fixed_amount, min_amount,
                       max_amount, quota_limit, valid_start, valid_end, condition_expression,
                       status, creator_id, create_time, update_time
                FROM gc_aid_plan
                WHERE is_deleted = 0
                """);
        if (student) {
            sql.append(" AND status = 1 AND valid_start <= CURRENT_DATE AND valid_end >= CURRENT_DATE");
            return jdbc.queryForList(sql.append(" ORDER BY update_time DESC").toString());
        }
        if (status != null) {
            sql.append(" AND status = ? ORDER BY update_time DESC");
            return jdbc.queryForList(sql.toString(), status);
        }
        return jdbc.queryForList(sql.append(" ORDER BY update_time DESC").toString());
    }

    @Transactional
    public long createPlan(AidPlanRequest request, long creatorId) {
        validatePlan(request);
        Map<String, Object> values = planValues(request);
        values.put("status", PLAN_DRAFT);
        values.put("creator_id", creatorId);
        values.put("is_deleted", 0);
        return new SimpleJdbcInsert(jdbc)
                .withTableName("gc_aid_plan")
                .usingGeneratedKeyColumns("id")
                .executeAndReturnKey(values)
                .longValue();
    }

    @Transactional
    public void updatePlan(long id, AidPlanRequest request) {
        validatePlan(request);
        requirePlanStatus(id, PLAN_DRAFT);
        int changed = jdbc.update("""
                        UPDATE gc_aid_plan
                           SET plan_name=?, fund_source=?, amount_mode=?, fixed_amount=?, min_amount=?,
                               max_amount=?, quota_limit=?, valid_start=?, valid_end=?,
                               condition_expression=?, update_time=NOW()
                         WHERE id=? AND is_deleted=0 AND status=0
                        """,
                request.planName().trim(), request.fundSource().trim(), normalizedMode(request.amountMode()),
                request.fixedAmount(), request.minAmount(), request.maxAmount(), request.quotaLimit(),
                request.validStart(), request.validEnd(), blankToNull(request.conditionExpression()), id);
        if (changed == 0) {
            throw new BusinessException(40900, "只有草稿方案可以修改");
        }
    }

    @Transactional
    public void changePlanStatus(long id, String action) {
        String normalized = action.toUpperCase(Locale.ROOT);
        int expected;
        int target;
        if ("PUBLISH".equals(normalized)) {
            expected = PLAN_DRAFT;
            target = PLAN_PUBLISHED;
        } else if ("OFFLINE".equals(normalized)) {
            expected = PLAN_PUBLISHED;
            target = PLAN_OFFLINE;
        } else {
            throw new BusinessException(40000, "方案动作仅支持 publish 或 offline");
        }
        int changed = jdbc.update("""
                UPDATE gc_aid_plan SET status=?, update_time=NOW()
                 WHERE id=? AND status=? AND is_deleted=0
                """, target, id, expected);
        if (changed == 0) {
            throw new BusinessException(40900, "方案状态已变化，请刷新后重试");
        }
    }

    public Map<String, Object> estimatePlan(long id) {
        requirePlanStatus(id, PLAN_DRAFT);
        Integer matched = jdbc.queryForObject(
                "SELECT COUNT(*) FROM gc_student WHERE is_deleted=0 AND status=1",
                Integer.class);
        Integer quota = jdbc.queryForObject(
                "SELECT quota_limit FROM gc_aid_plan WHERE id=? AND is_deleted=0",
                Integer.class, id);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("estimatedMatchedStudents", matched == null ? 0 : matched);
        result.put("quotaLimit", quota);
        result.put("estimatedCoverage", Math.min(matched == null ? 0 : matched, quota == null ? 0 : quota));
        result.put("note", "当前版本按有效学生总量给出上限估算；规则表达式将在联调数据完善后细化。");
        return result;
    }

    @Transactional
    public long createAppeal(AppealCreateRequest request, CurrentUser user) {
        long studentId = currentStudentId(user.id());
        SourceRejection rejection = findRejection(request.sourceType(), request.sourceApplyId(), studentId);
        int windowDays = appealWindowDays();
        if (rejection.rejectedAt().plusDays(windowDays).isBefore(LocalDateTime.now())) {
            throw new BusinessException(40900, "已超过" + windowDays + "天申诉窗口期");
        }
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("appeal_no", "AP" + System.currentTimeMillis());
        values.put("source_type", rejection.sourceType());
        values.put("source_apply_id", request.sourceApplyId());
        values.put("student_id", studentId);
        values.put("reason", request.reason().trim());
        values.put("attachment_ids", attachmentIdsJson(
                request.attachmentIds() == null ? List.of() : request.attachmentIds()));
        values.put("target_role", rejection.targetRole());
        values.put("status", 1);
        values.put("is_deleted", 0);
        long appealId;
        try {
            appealId = new SimpleJdbcInsert(jdbc)
                    .withTableName("gc_appeal")
                    .usingGeneratedKeyColumns("id")
                    .executeAndReturnKey(values)
                    .longValue();
        } catch (DuplicateKeyException exception) {
            throw new BusinessException(40900, "该申请已提交过申诉");
        }
        if (request.attachmentIds() != null && !request.attachmentIds().isEmpty()) {
            for (Long attachmentId : request.attachmentIds()) {
                int changed = jdbc.update("""
                        UPDATE gc_attachment
                           SET business_type='APPEAL', business_id=?, update_time=NOW()
                         WHERE id=? AND owner_id=? AND status='READY' AND is_deleted=0
                        """, appealId, attachmentId, user.id());
                if (changed == 0) {
                    throw new BusinessException(40000, "附件不存在或不属于当前用户");
                }
            }
        }
        return appealId;
    }

    public List<Map<String, Object>> myAppeals(long userId) {
        long studentId = currentStudentId(userId);
        return jdbc.queryForList("""
                SELECT id, appeal_no, source_type, source_apply_id, reason, attachment_ids,
                       target_role, status, conclusion, submit_time, handle_time, update_time
                  FROM gc_appeal
                 WHERE student_id=? AND is_deleted=0
                 ORDER BY submit_time DESC
                """, studentId);
    }

    public List<Map<String, Object>> pendingAppeals(CurrentUser user) {
        int reviewerRole = reviewerRole(user);
        return jdbc.queryForList("""
                SELECT a.id, a.appeal_no, a.source_type, a.source_apply_id, a.student_id,
                       s.student_no, s.name AS student_name, a.reason, a.attachment_ids,
                       a.target_role, a.status, a.submit_time, a.update_time
                  FROM gc_appeal a
                  JOIN gc_student s ON s.id=a.student_id AND s.is_deleted=0
                 WHERE a.target_role=? AND a.status IN (1,2) AND a.is_deleted=0
                 ORDER BY a.submit_time ASC
                """, reviewerRole);
    }

    @Transactional
    public void handleAppeal(long id, AppealHandleRequest request, CurrentUser user) {
        int reviewerRole = reviewerRole(user);
        String action = request.action().toUpperCase(Locale.ROOT);
        int targetStatus = switch (action) {
            case "ACCEPT" -> 2;
            case "UPHOLD" -> 3;
            case "REJECT" -> 4;
            case "RETURN" -> 5;
            default -> throw new BusinessException(40000, "处理动作仅支持 ACCEPT、UPHOLD、REJECT、RETURN");
        };
        int changed = jdbc.update("""
                UPDATE gc_appeal
                   SET status=?, conclusion=?, handler_id=?,
                       handle_time=CASE WHEN ? IN (3,4,5) THEN NOW() ELSE handle_time END,
                       update_time=NOW()
                 WHERE id=? AND target_role=? AND status IN (1,2) AND is_deleted=0
                """, targetStatus, request.conclusion().trim(), user.id(), targetStatus, id, reviewerRole);
        if (changed == 0) {
            throw new BusinessException(40900, "申诉不存在、已办结或不属于当前审核节点");
        }
    }

    @Transactional
    public long createSurvey(SurveyCreateRequest request, long creatorId) {
        if (request.startDate().isAfter(request.endDate())) {
            throw new BusinessException(40000, "问卷开始日期不能晚于结束日期");
        }
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("title", request.title().trim());
        values.put("target_type", request.targetType().toUpperCase(Locale.ROOT));
        values.put("target_batch_id", request.targetBatchId());
        values.put("start_date", request.startDate());
        values.put("end_date", request.endDate());
        values.put("status", 0);
        values.put("creator_id", creatorId);
        values.put("is_deleted", 0);
        return new SimpleJdbcInsert(jdbc)
                .withTableName("gc_satisfaction_survey")
                .usingGeneratedKeyColumns("id")
                .executeAndReturnKey(values)
                .longValue();
    }

    @Transactional
    public void publishSurvey(long id) {
        int changed = jdbc.update("""
                UPDATE gc_satisfaction_survey SET status=1, update_time=NOW()
                 WHERE id=? AND status=0 AND is_deleted=0
                """, id);
        if (changed == 0) {
            throw new BusinessException(40900, "问卷不存在或已经发布");
        }
    }

    public List<Map<String, Object>> listSurveys(CurrentUser user) {
        if (user.userType() == 1) {
            return jdbc.queryForList("""
                    SELECT q.id, q.title, q.target_type, q.target_batch_id, q.start_date, q.end_date,
                           q.status, r.score AS submitted_score, r.suggestion AS submitted_suggestion
                      FROM gc_satisfaction_survey q
                      LEFT JOIN gc_satisfaction_response r
                        ON r.survey_id=q.id AND r.student_id=? AND r.is_deleted=0
                     WHERE q.status=1 AND q.start_date<=CURRENT_DATE AND q.end_date>=CURRENT_DATE
                       AND q.is_deleted=0
                     ORDER BY q.create_time DESC
                    """, currentStudentId(user.id()));
        }
        return jdbc.queryForList("""
                SELECT id, title, target_type, target_batch_id, start_date, end_date,
                       status, creator_id, create_time, update_time
                  FROM gc_satisfaction_survey
                 WHERE is_deleted=0 ORDER BY create_time DESC
                """);
    }

    @Transactional
    public void submitSurvey(long surveyId, SurveyResponseRequest request, long userId) {
        Integer available = jdbc.queryForObject("""
                SELECT COUNT(*) FROM gc_satisfaction_survey
                 WHERE id=? AND status=1 AND start_date<=CURRENT_DATE AND end_date>=CURRENT_DATE
                   AND is_deleted=0
                """, Integer.class, surveyId);
        if (available == null || available == 0) {
            throw new BusinessException(40900, "问卷未发布或不在填写时间内");
        }
        try {
            jdbc.update("""
                    INSERT INTO gc_satisfaction_response
                           (survey_id, student_id, score, suggestion, is_deleted)
                    VALUES (?, ?, ?, ?, 0)
                    """, surveyId, currentStudentId(userId), request.score(), blankToNull(request.suggestion()));
        } catch (DuplicateKeyException exception) {
            throw new BusinessException(40900, "该问卷已经提交，不能重复填写");
        }
    }

    public Map<String, Object> surveySummary(long surveyId) {
        Integer exists = jdbc.queryForObject(
                "SELECT COUNT(*) FROM gc_satisfaction_survey WHERE id=? AND is_deleted=0",
                Integer.class, surveyId);
        if (exists == null || exists == 0) {
            throw new BusinessException(40400, "问卷不存在");
        }
        Map<String, Object> totals = jdbc.queryForMap("""
                SELECT COUNT(*) AS response_count,
                       COALESCE(ROUND(AVG(score),2),0) AS average_score,
                       SUM(CASE WHEN score=1 THEN 1 ELSE 0 END) AS score_1,
                       SUM(CASE WHEN score=2 THEN 1 ELSE 0 END) AS score_2,
                       SUM(CASE WHEN score=3 THEN 1 ELSE 0 END) AS score_3,
                       SUM(CASE WHEN score=4 THEN 1 ELSE 0 END) AS score_4,
                       SUM(CASE WHEN score=5 THEN 1 ELSE 0 END) AS score_5
                  FROM gc_satisfaction_response
                 WHERE survey_id=? AND is_deleted=0
                """, surveyId);
        List<String> suggestions = jdbc.queryForList("""
                SELECT suggestion FROM gc_satisfaction_response
                 WHERE survey_id=? AND suggestion IS NOT NULL AND suggestion<>'' AND is_deleted=0
                 ORDER BY create_time DESC LIMIT 100
                """, String.class, surveyId);
        Map<String, Integer> keywords = keywordFrequency(suggestions);
        Map<String, Object> result = new LinkedHashMap<>(totals);
        result.put("keywords", keywords);
        result.put("recentSuggestions", suggestions.stream().limit(20).toList());
        return result;
    }

    private void validatePlan(AidPlanRequest request) {
        if (request.validStart().isAfter(request.validEnd())) {
            throw new BusinessException(40000, "方案开始日期不能晚于结束日期");
        }
        String mode = normalizedMode(request.amountMode());
        if ("FIXED".equals(mode) && (request.fixedAmount() == null || request.fixedAmount().signum() <= 0)) {
            throw new BusinessException(40000, "固定金额方案必须填写大于0的固定金额");
        }
        if ("RANGE".equals(mode)
                && (request.minAmount() == null || request.maxAmount() == null
                || request.minAmount().signum() < 0
                || request.minAmount().compareTo(request.maxAmount()) > 0)) {
            throw new BusinessException(40000, "区间金额配置不合法");
        }
    }

    private String normalizedMode(String amountMode) {
        String mode = amountMode.toUpperCase(Locale.ROOT);
        if (!"FIXED".equals(mode) && !"RANGE".equals(mode)) {
            throw new BusinessException(40000, "金额模式仅支持 FIXED 或 RANGE");
        }
        return mode;
    }

    private Map<String, Object> planValues(AidPlanRequest request) {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("plan_name", request.planName().trim());
        values.put("fund_source", request.fundSource().trim());
        values.put("amount_mode", normalizedMode(request.amountMode()));
        values.put("fixed_amount", request.fixedAmount());
        values.put("min_amount", request.minAmount());
        values.put("max_amount", request.maxAmount());
        values.put("quota_limit", request.quotaLimit());
        values.put("valid_start", request.validStart());
        values.put("valid_end", request.validEnd());
        values.put("condition_expression", blankToNull(request.conditionExpression()));
        return values;
    }

    private void requirePlanStatus(long id, int status) {
        Integer count = jdbc.queryForObject("""
                SELECT COUNT(*) FROM gc_aid_plan
                 WHERE id=? AND status=? AND is_deleted=0
                """, Integer.class, id, status);
        if (count == null || count == 0) {
            throw new BusinessException(40400, "方案不存在或状态不允许当前操作");
        }
    }

    private long currentStudentId(long userId) {
        List<Long> ids = jdbc.queryForList("""
                SELECT id FROM gc_student WHERE user_id=? AND is_deleted=0 LIMIT 1
                """, Long.class, userId);
        if (ids.isEmpty()) {
            throw new BusinessException(40400, "当前账号未绑定学生档案");
        }
        return ids.get(0);
    }

    private SourceRejection findRejection(String requestedType, long applyId, long studentId) {
        String sourceType = requestedType.toUpperCase(Locale.ROOT);
        String sql;
        if ("SUBSIDY".equals(sourceType)) {
            sql = """
                    SELECT a.student_id, r.reviewer_role, r.review_time
                      FROM gc_subsidy_apply a
                      JOIN gc_subsidy_review r ON r.apply_id=a.id AND r.action=3 AND r.is_deleted=0
                     WHERE a.id=? AND a.student_id=? AND a.is_deleted=0
                     ORDER BY r.review_time DESC LIMIT 1
                    """;
        } else if ("GIFT".equals(sourceType)) {
            sql = """
                    SELECT a.student_id, r.reviewer_role, r.review_time
                      FROM gc_gift_pack_apply a
                      JOIN gc_review_record r
                        ON r.apply_id=a.id AND r.apply_type=1 AND r.action=3 AND r.is_deleted=0
                     WHERE a.id=? AND a.student_id=? AND a.is_deleted=0
                     ORDER BY r.review_time DESC LIMIT 1
                    """;
        } else {
            throw new BusinessException(40000, "申诉来源仅支持 SUBSIDY 或 GIFT");
        }
        List<Map<String, Object>> rows = jdbc.queryForList(sql, applyId, studentId);
        if (rows.isEmpty()) {
            throw new BusinessException(40900, "只能对本人已被“不通过”的申请发起申诉");
        }
        Map<String, Object> row = rows.get(0);
        Object time = row.get("review_time");
        LocalDateTime rejectedAt = time instanceof Timestamp timestamp
                ? timestamp.toLocalDateTime()
                : LocalDateTime.parse(String.valueOf(time).replace(' ', 'T'));
        return new SourceRejection(sourceType, ((Number) row.get("reviewer_role")).intValue(), rejectedAt);
    }

    private int appealWindowDays() {
        List<String> values = jdbc.queryForList("""
                SELECT config_value FROM gc_system_config
                 WHERE config_key='APPEAL_WINDOW_DAYS' AND is_deleted=0 LIMIT 1
                """, String.class);
        if (values.isEmpty()) {
            return 3;
        }
        try {
            return Math.max(1, Integer.parseInt(values.get(0)));
        } catch (NumberFormatException ignored) {
            return 3;
        }
    }

    private int reviewerRole(CurrentUser user) {
        return switch (user.userType()) {
            case 2 -> 1;
            case 3 -> 2;
            case 4, 5 -> 3;
            default -> throw new BusinessException(40300, "当前身份不能处理申诉");
        };
    }

    private String attachmentIdsJson(List<Long> attachmentIds) {
        return "[" + attachmentIds.stream()
                .map(String::valueOf)
                .collect(java.util.stream.Collectors.joining(",")) + "]";
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private Map<String, Integer> keywordFrequency(List<String> suggestions) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (String suggestion : suggestions) {
            String normalized = suggestion.replaceAll("[，。！？、；：,.!?;:\\s]+", " ");
            for (String token : normalized.split(" ")) {
                if (token.length() < 2) {
                    continue;
                }
                counts.merge(token, 1, Integer::sum);
            }
        }
        return counts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(20)
                .collect(
                        LinkedHashMap::new,
                        (map, entry) -> map.put(entry.getKey(), entry.getValue()),
                        LinkedHashMap::putAll);
    }

    private record SourceRejection(String sourceType, int targetRole, LocalDateTime rejectedAt) {
    }
}
