package edu.greenchannel.workstudy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import edu.greenchannel.common.BusinessException;
import edu.greenchannel.workstudy.entity.WorkStudyAttendance;
import edu.greenchannel.workstudy.entity.WorkStudyHire;
import edu.greenchannel.workstudy.mapper.WorkStudyAttendanceMapper;
import edu.greenchannel.workstudy.mapper.WorkStudyHireMapper;
import edu.greenchannel.workstudy.mapper.WorkStudyPositionMapper;
import edu.greenchannel.workstudy.service.WorkStudyAttendanceService;
import edu.greenchannel.workstudy.service.SystemConfigReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkStudyAttendanceServiceImpl
        extends ServiceImpl<WorkStudyAttendanceMapper, WorkStudyAttendance>
        implements WorkStudyAttendanceService {

    private final WorkStudyHireMapper hireMapper;
    private final WorkStudyPositionMapper positionMapper;
    private final SystemConfigReader configReader;

    // ==================== 1. 签到 ====================
    @Override
    @Transactional
    public Long checkIn(Long hireId, Long studentId, Integer checkType, String location) {
        validateHire(hireId, studentId);
        LocalDate today = LocalDate.now();
        if (checkType == null || (checkType != 1 && checkType != 2)) {
            throw new BusinessException(40000, "打卡方式仅支持定位或二维码");
        }
        if (location == null || location.isBlank()) {
            throw new BusinessException(40000, "打卡位置不能为空");
        }

        // 校验：同一天不能重复打卡
        if (existsToday(hireId, today)) {
            throw new BusinessException(40000, "今日已打卡");
        }

        WorkStudyAttendance attendance = new WorkStudyAttendance();
        attendance.setHireId(hireId);
        attendance.setStudentId(studentId);
        attendance.setAttendanceDate(today);
        attendance.setCheckInTime(LocalDateTime.now());
        attendance.setCheckType(checkType);
        attendance.setCheckInLocation(location);
        attendance.setStatus(1); // 默认正常，后续确认时调整
        attendance.setDeleted(0);
        attendance.setCreateTime(LocalDateTime.now());
        attendance.setUpdateTime(LocalDateTime.now());

        save(attendance);
        log.info("学生{}签到成功，hireId={}", studentId, hireId);
        return attendance.getId();
    }

    // ==================== 2. 签退 ====================
    @Override
    @Transactional
    public Long checkOut(Long attendanceId, Long studentId) {
        WorkStudyAttendance attendance = getById(attendanceId);
        validateAttendance(attendance, studentId);

        if (attendance.getCheckOutTime() != null) {
            throw new BusinessException(40000, "今日已签退");
        }

        LocalDateTime now = LocalDateTime.now();
        attendance.setCheckOutTime(now);

        // 计算工时
        BigDecimal hours = calculateWorkHours(
                attendance.getCheckInTime(), now);
        validateWeeklyHours(attendance.getHireId(), attendance.getAttendanceDate(), attendance.getId(), hours);
        attendance.setWorkHours(hours);

        attendance.setUpdateTime(LocalDateTime.now());
        updateById(attendance);

        log.info("学生{}签退成功，工时{}小时", studentId, hours);
        return attendance.getId();
    }

    // ==================== 3. 补打卡申请 ====================
    @Override
    @Transactional
    public Long applyRepair(Long hireId, Long studentId, LocalDate attendanceDate,
                            LocalDateTime checkInTime, LocalDateTime checkOutTime, String reason) {
        validateHire(hireId, studentId);

        if (!attendanceDate.isBefore(LocalDate.now())) {
            throw new BusinessException(40000, "只能申请补录过去日期的考勤");
        }
        BigDecimal repairHours = calculateWorkHours(checkInTime, checkOutTime);
        if (repairHours.signum() <= 0) {
            throw new BusinessException(40000, "补卡签退时间必须晚于签到时间");
        }
        validateWeeklyHours(hireId, attendanceDate, null, repairHours);

        // 校验：该日期没有正常打卡记录
        if (existsToday(hireId, attendanceDate)) {
            throw new BusinessException(40000, "该日期已有打卡记录");
        }

        WorkStudyAttendance attendance = new WorkStudyAttendance();
        attendance.setHireId(hireId);
        attendance.setStudentId(studentId);
        attendance.setAttendanceDate(attendanceDate);
        attendance.setCheckInTime(checkInTime);
        attendance.setCheckOutTime(checkOutTime);
        attendance.setWorkHours(repairHours);
        attendance.setStatus(6); // 6-补打卡待审批
        attendance.setRemark(reason);
        attendance.setDeleted(0);
        attendance.setCreateTime(LocalDateTime.now());
        attendance.setUpdateTime(LocalDateTime.now());

        save(attendance);
        log.info("学生{}提交补卡申请，日期{}", studentId, attendanceDate);
        return attendance.getId();
    }

    // ==================== 4. 部门确认 ====================
    @Override
    @Transactional
    public void confirmAttendance(Long attendanceId, Long confirmUserId, boolean canManageAll) {
        WorkStudyAttendance attendance = getById(attendanceId);
        if (attendance == null || attendance.getDeleted() == 1) {
            throw new BusinessException(40400, "考勤记录不存在");
        }

        // 只有待确认的记录才能确认（状态1-正常，或6-补打卡待审批）
        if (attendance.getStatus() != 1 && attendance.getStatus() != 6) {
            throw new BusinessException(40000, "该记录无需确认或已确认");
        }

        requirePositionManager(attendance.getHireId(), confirmUserId, canManageAll);

        attendance.setConfirmedBy(confirmUserId);
        attendance.setConfirmTime(LocalDateTime.now());
        attendance.setStatus(attendance.getStatus() == 6 ? 7 : 1); // 6→7, 1→1
        attendance.setUpdateTime(LocalDateTime.now());

        updateById(attendance);
        log.info("考勤确认完成：attendanceId={}, confirmUserId={}", attendanceId, confirmUserId);
    }

    // ==================== 5. 月度汇总 ====================
    @Override
    public AttendanceSummary summarizeMonthly(Long hireId, int year, int month) {
        LambdaQueryWrapper<WorkStudyAttendance> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WorkStudyAttendance::getHireId, hireId)
                .apply("YEAR(attendance_date) = {0} AND MONTH(attendance_date) = {1}", year, month)
                .in(WorkStudyAttendance::getStatus, 1, 7) // 只统计正常和补卡通过的
                .eq(WorkStudyAttendance::getDeleted, 0);

        var list = list(wrapper);

        BigDecimal totalHours = list.stream()
                .map(WorkStudyAttendance::getWorkHours)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 计算出勤天数（一天多次打卡算一天）
        long workDays = list.stream()
                .map(WorkStudyAttendance::getAttendanceDate)
                .distinct()
                .count();

        return new AttendanceSummary(totalHours, (int) workDays);
    }

    @Override
    public List<WorkStudyAttendance> listRecords(Long studentId, Long hireId, Integer status) {
        return lambdaQuery()
                .eq(studentId != null, WorkStudyAttendance::getStudentId, studentId)
                .eq(hireId != null, WorkStudyAttendance::getHireId, hireId)
                .eq(status != null, WorkStudyAttendance::getStatus, status)
                .eq(WorkStudyAttendance::getDeleted, 0)
                .orderByDesc(WorkStudyAttendance::getAttendanceDate)
                .orderByDesc(WorkStudyAttendance::getCreateTime)
                .list();
    }

    @Override
    public List<WorkStudyAttendance> listManageableRecords(
            Long studentId, Long hireId, Integer status, Long operatorId, boolean canManageAll) {
        List<WorkStudyAttendance> records = listRecords(studentId, hireId, status);
        if (canManageAll) {
            return records;
        }
        return records.stream().filter(record -> {
            WorkStudyHire hire = hireMapper.selectById(record.getHireId());
            if (hire == null || hire.getDeleted() == 1) {
                return false;
            }
            var position = positionMapper.selectById(hire.getPositionId());
            return position != null && position.getDeleted() != 1
                    && operatorId.equals(position.getPublisherId());
        }).toList();
    }

    // ==================== 私有方法 ====================

    private void validateHire(Long hireId, Long studentId) {
        WorkStudyHire hire = hireMapper.selectById(hireId);
        if (hire == null || hire.getDeleted() == 1) {
            throw new BusinessException(40400, "录用记录不存在");
        }
        if (!hire.getStudentId().equals(studentId)) {
            throw new BusinessException(40300, "无权操作此录用记录");
        }
        if (hire.getHireStatus() != 1) { // 1-在岗
            throw new BusinessException(40000, "当前状态不允许打卡");
        }
    }

    private void requirePositionManager(Long hireId, Long operatorId, boolean canManageAll) {
        if (canManageAll) {
            return;
        }
        WorkStudyHire hire = hireMapper.selectById(hireId);
        if (hire == null || hire.getDeleted() == 1) {
            throw new BusinessException(40400, "录用记录不存在");
        }
        var position = positionMapper.selectById(hire.getPositionId());
        if (position == null || position.getDeleted() == 1
                || !operatorId.equals(position.getPublisherId())) {
            throw new BusinessException(40300, "无权确认其他部门的考勤记录");
        }
    }

    private void validateAttendance(WorkStudyAttendance attendance, Long studentId) {
        if (attendance == null || attendance.getDeleted() == 1) {
            throw new BusinessException(40400, "考勤记录不存在");
        }
        if (!attendance.getStudentId().equals(studentId)) {
            throw new BusinessException(40300, "无权操作此考勤记录");
        }
        if (attendance.getCheckInTime() == null) {
            throw new BusinessException(40000, "请先签到");
        }
    }

    private boolean existsToday(Long hireId, LocalDate date) {
        return lambdaQuery()
                .eq(WorkStudyAttendance::getHireId, hireId)
                .eq(WorkStudyAttendance::getAttendanceDate, date)
                .eq(WorkStudyAttendance::getDeleted, 0)
                .exists();
    }

    private void validateWeeklyHours(
            Long hireId,
            LocalDate attendanceDate,
            Long excludedAttendanceId,
            BigDecimal hoursToAdd) {
        LocalDate weekStart = attendanceDate.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
        LocalDate weekEnd = attendanceDate.with(TemporalAdjusters.nextOrSame(java.time.DayOfWeek.SUNDAY));
        List<WorkStudyAttendance> weeklyRecords = lambdaQuery()
                .eq(WorkStudyAttendance::getHireId, hireId)
                .between(WorkStudyAttendance::getAttendanceDate, weekStart, weekEnd)
                .ne(excludedAttendanceId != null, WorkStudyAttendance::getId, excludedAttendanceId)
                .in(WorkStudyAttendance::getStatus, 1, 7)
                .eq(WorkStudyAttendance::getDeleted, 0)
                .list();
        BigDecimal existingHours = weeklyRecords.stream()
                .map(record -> record.getWorkHours() == null ? BigDecimal.ZERO : record.getWorkHours())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal maxWeeklyHours = configReader.positiveDecimal(
                "WORKSTUDY_MAX_WEEKLY_HOURS", BigDecimal.valueOf(8));
        if (existingHours.add(hoursToAdd).compareTo(maxWeeklyHours) > 0) {
            throw new BusinessException(
                    40900, "本周累计工时不得超过" + maxWeeklyHours.stripTrailingZeros().toPlainString() + "小时");
        }
    }

    private BigDecimal calculateWorkHours(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null || end.isBefore(start)) {
            return BigDecimal.ZERO;
        }

        long minutes = Duration.between(start, end).toMinutes();
        // 扣除午休时间（12:00-13:30，共90分钟）
        if (start.toLocalTime().isBefore(LocalTime.of(12, 0)) &&
                end.toLocalTime().isAfter(LocalTime.of(13, 30))) {
            minutes -= 90;
        }

        return BigDecimal.valueOf(Math.max(minutes, 0))
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
    }
}
