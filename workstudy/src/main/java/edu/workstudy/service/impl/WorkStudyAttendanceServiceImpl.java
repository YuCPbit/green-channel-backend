package edu.workstudy.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.workstudy.entity.WorkStudyAttendance;
import edu.workstudy.mapper.WorkStudyAttendanceMapper;
import edu.workstudy.service.WorkStudyAttendanceService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Duration;

@Service
public class WorkStudyAttendanceServiceImpl
        extends ServiceImpl<WorkStudyAttendanceMapper, WorkStudyAttendance>
        implements WorkStudyAttendanceService {

    @Override
    public Long checkIn(Long hireId, Long studentId, String location) {
        // 校验：同一天不能重复签到
        long count = lambdaQuery()
                .eq(WorkStudyAttendance::getHireId, hireId)
                .eq(WorkStudyAttendance::getAttendanceDate, LocalDate.now())
                .count();
        if (count > 0) {
            throw new RuntimeException("今日已签到，请勿重复操作");
        }

        WorkStudyAttendance attendance = new WorkStudyAttendance();
        attendance.setHireId(hireId);
        attendance.setStudentId(studentId);
        attendance.setAttendanceDate(LocalDate.now());
        attendance.setCheckInTime(LocalDateTime.now());
        attendance.setCheckInLocation(location);
        attendance.setStatus(1); // 1-正常
        attendance.setCreateTime(LocalDateTime.now());

        save(attendance);
        return attendance.getId();
    }

    @Override
    public void checkOut(Long attendanceId) {
        WorkStudyAttendance attendance = getById(attendanceId);
        if (attendance == null) {
            throw new RuntimeException("考勤记录不存在");
        }
        if (attendance.getCheckOutTime() != null) {
            throw new RuntimeException("今日已签退");
        }

        attendance.setCheckOutTime(LocalDateTime.now());

        // 计算工时（小时，保留两位小数）
        long minutes = Duration.between(
                attendance.getCheckInTime(),
                attendance.getCheckOutTime()
        ).toMinutes();

        BigDecimal workHours = BigDecimal.valueOf(minutes)
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);

        attendance.setWorkHours(workHours);
        updateById(attendance);
    }
}