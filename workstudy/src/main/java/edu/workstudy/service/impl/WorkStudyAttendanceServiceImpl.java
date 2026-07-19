package edu.workstudy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.workstudy.entity.WorkStudyAttendance;
import edu.workstudy.mapper.WorkStudyAttendanceMapper;
import edu.workstudy.service.WorkStudyAttendanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkStudyAttendanceServiceImpl
        extends ServiceImpl<WorkStudyAttendanceMapper, WorkStudyAttendance>
        implements WorkStudyAttendanceService {

    private static final LocalTime WORK_START_TIME = LocalTime.of(8, 30); // 上班时间
    private static final LocalTime WORK_END_TIME = LocalTime.of(17, 30);   // 下班时间
    private static final int LATE_THRESHOLD_MINUTES = 15; // 15分钟内不算迟到

    // ==================== 1. 签到 ====================
    @Override
    @Transactional
    public Long checkIn(Long hireId, Long studentId, String location) {
        LocalDate today = LocalDate.now();

        // 校验：同一天不能重复签到
        if (existsToday(hireId, today)) {
            throw new RuntimeException("今日已签到，请勿重复操作");
        }

        // 校验：周末不允许打卡（可选）
        if (today.getDayOfWeek() == DayOfWeek.SATURDAY ||
                today.getDayOfWeek() == DayOfWeek.SUNDAY) {
            throw new RuntimeException("周末无需打卡");
        }

        WorkStudyAttendance attendance = new WorkStudyAttendance();
        attendance.setHireId(hireId);
        attendance.setStudentId(studentId);
        attendance.setAttendanceDate(today);
        attendance.setCheckInTime(LocalDateTime.now());
        attendance.setCheckInLocation(location);
        attendance.setAttendanceType(1); // 1-打卡
        attendance.setApprovalStatus(0); // 无需审批

        // 计算是否迟到
        int lateMinutes = calculateLateMinutes(LocalDateTime.now());
        attendance.setLateMinutes(lateMinutes);
        attendance.setStatus(determineStatus(lateMinutes, 0));

        save(attendance);
        log.info("学生{}签到成功，迟到{}分钟", studentId, lateMinutes);
        return attendance.getId();
    }

    // ==================== 2. 签退 ====================
    @Override
    @Transactional
    public void checkOut(Long attendanceId) {
        WorkStudyAttendance attendance = getById(attendanceId);
        validateAttendanceExists(attendance);

        if (attendance.getCheckOutTime() != null) {
            throw new RuntimeException("今日已签退");
        }

        LocalDateTime now = LocalDateTime.now();
        attendance.setCheckOutTime(now);

        // 计算早退分钟数
        int earlyMinutes = calculateEarlyMinutes(now);
        attendance.setEarlyMinutes(earlyMinutes);

        // 计算工时
        attendance.setWorkHours(calculateWorkHours(
                attendance.getCheckInTime(), now,
                attendance.getLateMinutes(), earlyMinutes));

        // 更新状态
        attendance.setStatus(determineStatus(attendance.getLateMinutes(), earlyMinutes));

        updateById(attendance);
        log.info("学生{}签退成功，工时{}小时", attendance.getStudentId(), attendance.getWorkHours());
    }

    // ==================== 3. 请假申请 ====================
    @Override
    @Transactional
    public Long applyLeave(Long hireId, Long studentId, LocalDate leaveDate,
                           Integer leaveType, String reason) {
        // 校验：当天不能有打卡记录
        if (existsToday(hireId, leaveDate)) {
            throw new RuntimeException("当天已有打卡记录，无法请假");
        }

        WorkStudyAttendance attendance = new WorkStudyAttendance();
        attendance.setHireId(hireId);
        attendance.setStudentId(studentId);
        attendance.setAttendanceDate(leaveDate);
        attendance.setAttendanceType(2); // 2-请假
        attendance.setLeaveType(leaveType);
        attendance.setRemark(reason);
        attendance.setApprovalStatus(1); // 待审批
        attendance.setWorkHours(BigDecimal.ZERO);
        attendance.setStatus(4); // 4-请假

        save(attendance);
        log.info("学生{}提交请假申请，日期{}", studentId, leaveDate);
        return attendance.getId();
    }

    // ==================== 4. 补卡申请 ====================
    @Override
    @Transactional
    public Long applyRepair(Long hireId, Long studentId, LocalDate attendanceDate,
                            LocalDateTime checkInTime, LocalDateTime checkOutTime,
                            String reason) {
        // 校验：当天不能有正常打卡
        if (existsToday(hireId, attendanceDate)) {
            throw new RuntimeException("当天已有打卡记录");
        }

        WorkStudyAttendance attendance = new WorkStudyAttendance();
        attendance.setHireId(hireId);
        attendance.setStudentId(studentId);
        attendance.setAttendanceDate(attendanceDate);
        attendance.setCheckInTime(checkInTime);
        attendance.setCheckOutTime(checkOutTime);
        attendance.setAttendanceType(3); // 3-补卡
        attendance.setRemark(reason);
        attendance.setApprovalStatus(1); // 待审批

        // 计算补卡的工时
        attendance.setWorkHours(calculateWorkHours(
                checkInTime, checkOutTime, 0, 0));

        save(attendance);
        log.info("学生{}提交补卡申请，日期{}", studentId, attendanceDate);
        return attendance.getId();
    }

    // ==================== 5. 审批（请假/补卡） ====================
    @Override
    @Transactional
    public void approveAttendance(Long attendanceId, Long approverId, boolean approved) {
        WorkStudyAttendance attendance = getById(attendanceId);
        validateAttendanceExists(attendance);

        if (attendance.getApprovalStatus() != 1) {
            throw new RuntimeException("该申请已审批或无需审批");
        }

        attendance.setApproverId(approverId);
        attendance.setApproveTime(LocalDateTime.now());

        if (approved) {
            attendance.setApprovalStatus(2); // 通过
            // 补卡通过后，更新状态为正常
            if (attendance.getAttendanceType() == 3) {
                attendance.setStatus(1);
            }
        } else {
            attendance.setApprovalStatus(3); // 驳回
        }

        updateById(attendance);
        log.info("审批完成：attendanceId={}, 结果={}", attendanceId, approved ? "通过" : "驳回");
    }

    // ==================== 6. 月度考勤统计（给薪酬模块用） ====================
    @Override
    public AttendanceSummary summarizeMonthly(Long hireId, int year, int month) {
        LambdaQueryWrapper<WorkStudyAttendance> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WorkStudyAttendance::getHireId, hireId)
                .apply("YEAR(attendance_date) = {0} AND MONTH(attendance_date) = {1}", year, month)
                .eq(WorkStudyAttendance::getApprovalStatus, 2);

        var list = list(wrapper);

        BigDecimal totalHours = list.stream()
                .map(WorkStudyAttendance::getWorkHours)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 与核算逻辑保持一致
        long workDays = list.stream()
                .filter(a -> a.getStatus() == 1 || a.getStatus() == 2 || a.getStatus() == 3 || a.getStatus() == 5)
                .count();

        return new AttendanceSummary(totalHours, (int) workDays);
    }

    // ==================== 私有方法 ====================

    private boolean existsToday(Long hireId, LocalDate date) {
        return lambdaQuery()
                .eq(WorkStudyAttendance::getHireId, hireId)
                .eq(WorkStudyAttendance::getAttendanceDate, date)
                .exists();
    }

    private void validateAttendanceExists(WorkStudyAttendance attendance) {
        if (attendance == null) {
            throw new RuntimeException("考勤记录不存在");
        }
    }

    private int calculateLateMinutes(LocalDateTime checkInTime) {
        LocalTime inTime = checkInTime.toLocalTime();
        if (inTime.isBefore(WORK_START_TIME.plusMinutes(LATE_THRESHOLD_MINUTES))) {
            return 0;
        }
        return (int) Duration.between(WORK_START_TIME, inTime).toMinutes();
    }

    private int calculateEarlyMinutes(LocalDateTime checkOutTime) {
        LocalTime outTime = checkOutTime.toLocalTime();
        if (outTime.isAfter(WORK_END_TIME.minusMinutes(LATE_THRESHOLD_MINUTES))) {
            return 0;
        }
        return (int) Duration.between(outTime, WORK_END_TIME).toMinutes();
    }

    private BigDecimal calculateWorkHours(LocalDateTime start, LocalDateTime end,
                                          int lateMinutes, int earlyMinutes) {
        long totalMinutes = Duration.between(start, end).toMinutes();
        long actualMinutes = totalMinutes - lateMinutes - earlyMinutes;
        return BigDecimal.valueOf(Math.max(actualMinutes, 0))
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
    }

    private Integer determineStatus(int lateMinutes, int earlyMinutes) {
        if (lateMinutes > 0 && earlyMinutes > 0) return 3; // 迟到+早退
        if (lateMinutes > 0) return 2; // 迟到
        if (earlyMinutes > 0) return 5; // 早退
        return 1; // 正常
    }
}