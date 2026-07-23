package edu.greenchannel.workstudy.controller;

import edu.greenchannel.auth.AuthInterceptor;
import edu.greenchannel.auth.CurrentUser;
import edu.greenchannel.auth.RequirePermission;
import edu.greenchannel.common.ApiResponse;
import edu.greenchannel.workstudy.service.WorkStudyAttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/workstudy/attendance")
@RequiredArgsConstructor
public class WorkStudyAttendanceController {

    private final WorkStudyAttendanceService attendanceService;

    /**
     * 学生签到
     */
    @PostMapping("/check-in")
    @RequirePermission("workstudy:attendance:checkin")
    public ApiResponse<Long> checkIn(@RequestParam Long hireId,
                                     @RequestParam Integer checkType,
                                     @RequestParam String location,
                                     @RequestAttribute(AuthInterceptor.CURRENT_USER_ATTRIBUTE) CurrentUser currentUser) {
        Long attendanceId = attendanceService.checkIn(hireId, currentUser.id(), checkType, location);
        return ApiResponse.success(attendanceId);
    }

    /**
     * 学生签退
     */
    @PostMapping("/check-out")
    @RequirePermission("workstudy:attendance:checkout")
    public ApiResponse<Long> checkOut(@RequestParam Long attendanceId,
                                      @RequestAttribute(AuthInterceptor.CURRENT_USER_ATTRIBUTE) CurrentUser currentUser) {
        Long id = attendanceService.checkOut(attendanceId, currentUser.id());
        return ApiResponse.success(id);
    }

    /**
     * 补打卡申请
     */
    @PostMapping("/repair")
    @RequirePermission("workstudy:attendance:repair")
    public ApiResponse<Long> applyRepair(@RequestParam Long hireId,
                                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate attendanceDate,
                                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime checkInTime,
                                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime checkOutTime,
                                         @RequestParam String reason,
                                         @RequestAttribute(AuthInterceptor.CURRENT_USER_ATTRIBUTE) CurrentUser currentUser) {
        Long attendanceId = attendanceService.applyRepair(hireId, currentUser.id(),
                attendanceDate, checkInTime, checkOutTime, reason);
        return ApiResponse.success(attendanceId);
    }

    /**
     * 用工部门确认考勤
     */
    @PostMapping("/{attendanceId}/confirm")
    @RequirePermission("workstudy:attendance:confirm")
    public ApiResponse<Void> confirmAttendance(@PathVariable Long attendanceId,
                                               @RequestAttribute(AuthInterceptor.CURRENT_USER_ATTRIBUTE) CurrentUser currentUser) {
        attendanceService.confirmAttendance(attendanceId, currentUser.id());
        return ApiResponse.success();
    }
}