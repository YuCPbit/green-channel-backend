package edu.greenchannel.workstudy.service;

import edu.greenchannel.auth.CurrentUser;
import edu.greenchannel.common.BusinessException;
import edu.greenchannel.workstudy.controller.WorkStudyMovementController.MovementCreateRequest;
import edu.greenchannel.workstudy.controller.WorkStudyMovementController.MovementReviewRequest;
import edu.greenchannel.workstudy.entity.WorkStudyHire;
import edu.greenchannel.workstudy.mapper.WorkStudyHireMapper;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WorkStudyMovementService {

    private final JdbcTemplate jdbc;
    private final WorkStudyHireMapper hireMapper;
    private final WorkStudyAgreementService agreementService;

    public List<Map<String, Object>> availablePositions() {
        return jdbc.queryForList("""
                SELECT id, position_name, department_name, work_location, salary_type,
                       salary_rate, recruit_count, hired_count
                  FROM gc_work_study_position
                 WHERE status=2 AND hired_count<recruit_count AND is_deleted=0
                 ORDER BY department_name, position_name
                """);
    }

    @Transactional
    public long create(MovementCreateRequest request, CurrentUser user) {
        String movementType = request.movementType().toUpperCase(Locale.ROOT);
        if (!List.of("TRANSFER", "LEAVE").contains(movementType)) {
            throw new BusinessException(40000, "岗位变动类型仅支持 TRANSFER 或 LEAVE");
        }
        List<Map<String, Object>> hires = jdbc.queryForList("""
                SELECT h.id, h.student_id, h.position_id
                  FROM gc_work_study_hire h
                  JOIN gc_student s ON s.id=h.student_id AND s.is_deleted=0
                 WHERE h.id=? AND h.hire_status=1 AND h.is_deleted=0 AND s.user_id=?
                """, request.hireId(), user.id());
        if (hires.isEmpty()) {
            throw new BusinessException(40400, "未找到本人当前在岗记录");
        }
        Map<String, Object> hire = hires.get(0);
        Long oldPositionId = ((Number) hire.get("position_id")).longValue();
        if ("TRANSFER".equals(movementType)) {
            if (request.targetPositionId() == null || request.targetPositionId().equals(oldPositionId)) {
                throw new BusinessException(40000, "调岗必须选择不同的目标岗位");
            }
            Integer available = jdbc.queryForObject("""
                    SELECT COUNT(*) FROM gc_work_study_position
                     WHERE id=? AND status=2 AND hired_count<recruit_count AND is_deleted=0
                    """, Integer.class, request.targetPositionId());
            if (available == null || available == 0) {
                throw new BusinessException(40900, "目标岗位已下架或名额已满");
            }
        }
        Integer pending = jdbc.queryForObject("""
                SELECT COUNT(*) FROM gc_work_study_movement
                 WHERE hire_id=? AND status=1 AND is_deleted=0
                """, Integer.class, request.hireId());
        if (pending != null && pending > 0) {
            throw new BusinessException(40900, "该在岗记录已有待审批的变动申请");
        }
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("movement_no", "WM" + System.currentTimeMillis());
        values.put("hire_id", request.hireId());
        values.put("student_id", hire.get("student_id"));
        values.put("from_position_id", oldPositionId);
        values.put("to_position_id", request.targetPositionId());
        values.put("movement_type", movementType);
        values.put("reason", request.reason().trim());
        values.put("applicant_user_id", user.id());
        values.put("status", 1);
        values.put("is_deleted", 0);
        return new SimpleJdbcInsert(jdbc)
                .withTableName("gc_work_study_movement")
                .usingGeneratedKeyColumns("id")
                .executeAndReturnKey(values)
                .longValue();
    }

    public List<Map<String, Object>> myMovements(long userId) {
        return jdbc.queryForList("""
                SELECT m.id, m.movement_no, m.movement_type, m.reason, m.status,
                       m.review_comment, m.apply_time, m.review_time,
                       oldp.position_name AS from_position_name,
                       newp.position_name AS to_position_name
                  FROM gc_work_study_movement m
                  JOIN gc_student s ON s.id=m.student_id AND s.user_id=? AND s.is_deleted=0
                  JOIN gc_work_study_position oldp ON oldp.id=m.from_position_id
                  LEFT JOIN gc_work_study_position newp ON newp.id=m.to_position_id
                 WHERE m.is_deleted=0
                 ORDER BY m.apply_time DESC
                """, userId);
    }

    public List<Map<String, Object>> pendingMovements() {
        return jdbc.queryForList("""
                SELECT m.id, m.movement_no, m.movement_type, m.reason, m.apply_time,
                       s.student_no, s.name AS student_name,
                       oldp.position_name AS from_position_name,
                       newp.position_name AS to_position_name
                  FROM gc_work_study_movement m
                  JOIN gc_student s ON s.id=m.student_id AND s.is_deleted=0
                  JOIN gc_work_study_position oldp ON oldp.id=m.from_position_id
                  LEFT JOIN gc_work_study_position newp ON newp.id=m.to_position_id
                 WHERE m.status=1 AND m.is_deleted=0
                 ORDER BY m.apply_time ASC
                """);
    }

    @Transactional
    public void review(long id, MovementReviewRequest request, long reviewerId) {
        String action = request.action().toUpperCase(Locale.ROOT);
        if (!List.of("APPROVE", "REJECT").contains(action)) {
            throw new BusinessException(40000, "审批动作仅支持 APPROVE 或 REJECT");
        }
        List<Map<String, Object>> rows = jdbc.queryForList("""
                SELECT id, hire_id, student_id, from_position_id, to_position_id,
                       movement_type, reason
                  FROM gc_work_study_movement
                 WHERE id=? AND status=1 AND is_deleted=0
                 FOR UPDATE
                """, id);
        if (rows.isEmpty()) {
            throw new BusinessException(40900, "变动申请不存在或已经处理");
        }
        if ("REJECT".equals(action)) {
            jdbc.update("""
                    UPDATE gc_work_study_movement
                       SET status=3, reviewer_id=?, review_comment=?, review_time=NOW(), update_time=NOW()
                     WHERE id=? AND status=1
                    """, reviewerId, request.comment().trim(), id);
            return;
        }
        Map<String, Object> movement = rows.get(0);
        long hireId = ((Number) movement.get("hire_id")).longValue();
        long fromPositionId = ((Number) movement.get("from_position_id")).longValue();
        String type = String.valueOf(movement.get("movement_type"));
        WorkStudyHire oldHire = hireMapper.selectById(hireId);
        if (oldHire == null || oldHire.getHireStatus() != 1) {
            throw new BusinessException(40900, "原在岗关系已经变化，无法审批");
        }
        if ("TRANSFER".equals(type)) {
            Number targetValue = (Number) movement.get("to_position_id");
            if (targetValue == null) {
                throw new BusinessException(40000, "调岗申请缺少目标岗位");
            }
            long targetPositionId = targetValue.longValue();
            int occupied = jdbc.update("""
                    UPDATE gc_work_study_position
                       SET hired_count=hired_count+1, update_time=NOW()
                     WHERE id=? AND status=2 AND hired_count<recruit_count AND is_deleted=0
                    """, targetPositionId);
            if (occupied == 0) {
                throw new BusinessException(40900, "目标岗位已下架或名额已满");
            }
            releasePosition(fromPositionId);
            jdbc.update("""
                    UPDATE gc_work_study_hire
                       SET hire_status=2, leave_date=CURRENT_DATE, leave_reason=?, update_time=NOW()
                     WHERE id=? AND hire_status=1 AND is_deleted=0
                    """, "调岗：" + movement.get("reason"), hireId);
            jdbc.update("""
                    INSERT INTO gc_work_study_hire
                           (apply_id, position_id, student_id, hire_status, hire_date,
                            approved_by, approve_time, salary_rate, is_deleted)
                    SELECT apply_id, ?, student_id, 1, CURRENT_DATE, ?, NOW(), p.salary_rate, 0
                      FROM gc_work_study_hire h
                      JOIN gc_work_study_position p ON p.id=?
                     WHERE h.id=?
                    """, targetPositionId, reviewerId, targetPositionId, hireId);
            Long newHireId = jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
            if (newHireId != null) {
                WorkStudyHire newHire = hireMapper.selectById(newHireId);
                agreementService.generateAgreement(newHire);
            }
        } else {
            jdbc.update("""
                    UPDATE gc_work_study_hire
                       SET hire_status=3, leave_date=CURRENT_DATE, leave_reason=?, update_time=NOW()
                     WHERE id=? AND hire_status=1 AND is_deleted=0
                    """, movement.get("reason"), hireId);
            releasePosition(fromPositionId);
        }
        jdbc.update("""
                UPDATE gc_work_study_movement
                   SET status=2, reviewer_id=?, review_comment=?, review_time=NOW(), update_time=NOW()
                 WHERE id=? AND status=1
                """, reviewerId, request.comment().trim(), id);
    }

    private void releasePosition(long positionId) {
        jdbc.update("""
                UPDATE gc_work_study_position
                   SET hired_count=GREATEST(hired_count-1,0),
                       status=CASE WHEN status=3 AND hired_count-1<recruit_count THEN 2 ELSE status END,
                       update_time=NOW()
                 WHERE id=? AND is_deleted=0
                """, positionId);
    }
}
