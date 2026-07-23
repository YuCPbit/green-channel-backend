package edu.greenchannel.workstudy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.greenchannel.workstudy.entity.WorkStudyAttendance;
import org.springframework.lang.NonNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public interface WorkStudyAttendanceService extends IService<WorkStudyAttendance> {

    // ==================== 1. 学生打卡 ====================
    Long checkIn(@NonNull Long hireId, @NonNull Long studentId,
                 @NonNull Integer checkType, @NonNull String location);

    Long checkOut(@NonNull Long attendanceId, @NonNull Long studentId);

    // ==================== 2. 补打卡申请 ====================
    Long applyRepair(@NonNull Long hireId, @NonNull Long studentId,
                     @NonNull LocalDate attendanceDate,
                     @NonNull LocalDateTime checkInTime,
                     @NonNull LocalDateTime checkOutTime,
                     @NonNull String reason);

    // ==================== 3. 部门确认（替代审批） ====================
    void confirmAttendance(@NonNull Long attendanceId, @NonNull Long confirmUserId);

    // ==================== 4. 月度汇总（给薪酬用） ====================
    AttendanceSummary summarizeMonthly(@NonNull Long hireId, int year, int month);

    // ==================== 5. 内部DTO ====================
    record AttendanceSummary(@NonNull BigDecimal totalHours, int workDays) {}
}