package edu.greenchannel.workstudy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import edu.greenchannel.common.BusinessException;
import edu.greenchannel.workstudy.entity.WorkStudyAttendance;
import edu.greenchannel.workstudy.entity.WorkStudyHire;
import edu.greenchannel.workstudy.mapper.WorkStudyAttendanceMapper;
import edu.greenchannel.workstudy.mapper.WorkStudyHireMapper;
import edu.greenchannel.workstudy.service.WorkStudyAttendanceService;
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

    private final WorkStudyHireMapper hireMapper;

    // 系统参数（后续从配置表读取）
    private static final LocalTime WORK_START = LocalTime.of(8, 30);
    private static final LocalTime WORK_END = LocalTime.of(17, 30);
    private static final int MAX_DAILY_HOURS = 8; // WS_MAX_WEEKLY_HOURS / 5

    // ==================== 1. 签到 ====================
    @Override
    @Transactional
    public Long checkIn(Long hireId, Long studentId, Integer checkType, String location) {
        validateHire(hireId, studentId);
        LocalDate today = LocalDate.now();

        // 校验：同一天不能重复打卡
        if (existsToday(hireId, today)) {
            throw new BusinessException(40000, "今日已打卡");
        }

        // 校验：周末不允许打卡（勤工助学原则上不安排周末）
        if (isWeekend(today)) {
            throw new BusinessException(40000, "周末无需打卡");
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
        attendance.setWorkHours(hours.min(BigDecimal.valueOf(MAX_DAILY_HOURS)));

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

        // 校验：不能补当天的卡
        if (attendanceDate.equals(LocalDate.now())) {
            throw new BusinessException(40000, "当日考勤请在当日处理");
        }

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
        attendance.setWorkHours(calculateWorkHours(checkInTime, checkOutTime));
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
    public void confirmAttendance(Long attendanceId, Long confirmUserId) {
        WorkStudyAttendance attendance = getById(attendanceId);
        if (attendance == null || attendance.getDeleted() == 1) {
            throw new BusinessException(40400, "考勤记录不存在");
        }

        // 只有待确认的记录才能确认（状态1-正常，或6-补打卡待审批）
        if (attendance.getStatus() != 1 && attendance.getStatus() != 6) {
            throw new BusinessException(40000, "该记录无需确认或已确认");
        }

        // TODO: 校验confirmUserId是否是该岗位的部门负责人

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

    private boolean isWeekend(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
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