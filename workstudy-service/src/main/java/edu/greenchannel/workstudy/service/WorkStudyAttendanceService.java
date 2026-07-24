package edu.greenchannel.workstudy.service;

import com.baomidou.mybatisplus.spring.service.IService;
import edu.greenchannel.workstudy.entity.WorkStudyAttendance;
import org.springframework.lang.NonNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 考勤服务接口
 */
public interface WorkStudyAttendanceService extends IService<WorkStudyAttendance> {

    // ==================== 1. 签到 ====================
    Long checkIn(@NonNull Long hireId,
                 @NonNull Long studentId,
                 @NonNull String location);

    // ==================== 2. 签退 ====================
    void checkOut(@NonNull Long attendanceId, @NonNull Long studentId);

    // ==================== 3. 请假申请 ====================
    Long applyLeave(@NonNull Long hireId,
                    @NonNull Long studentId,
                    @NonNull LocalDate leaveDate,
                    @NonNull Integer leaveType,
                    @NonNull String reason);

    // ==================== 4. 补卡申请 ====================
    Long applyRepair(@NonNull Long hireId,
                     @NonNull Long studentId,
                     @NonNull LocalDate attendanceDate,
                     @NonNull LocalDateTime checkInTime,
                     @NonNull LocalDateTime checkOutTime,
                     @NonNull String reason);

    // ==================== 5. 审批 ====================
    void approveAttendance(@NonNull Long attendanceId,
                           @NonNull Long approverId,
                           boolean approved);

    // ==================== 6. 月度汇总（给薪酬用） ====================
    AttendanceSummary summarizeMonthly(@NonNull Long hireId,
                                       int year,
                                       int month);

    // ==================== 7. 内部 DTO ====================
    record AttendanceSummary(@NonNull BigDecimal totalHours,
                             int workDays) {}
}
