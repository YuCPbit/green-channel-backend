package edu.greenchannel.workstudy.controller;

import edu.greenchannel.common.ApiResponse;
import edu.greenchannel.auth.AuthInterceptor;
import edu.greenchannel.auth.CurrentUser;
import edu.greenchannel.auth.RequirePermission;
import edu.greenchannel.workstudy.service.WorkStudyAttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class WorkStudyAttendanceController {

    private final WorkStudyAttendanceService attendanceService;

    @PostMapping("/check-in")
    @RequirePermission("student:workstudy:view")
    public ApiResponse<Long> checkIn(@RequestParam Long hireId,
                                @RequestAttribute(AuthInterceptor.CURRENT_USER_ATTRIBUTE) CurrentUser currentUser,
                                @RequestParam String location) {
        Long attendanceId = attendanceService.checkIn(hireId, currentUser.id(), location);
        return ApiResponse.success(attendanceId);
    }

    @PostMapping("/check-out")
    @RequirePermission("student:workstudy:view")
    public ApiResponse<Void> checkOut(@RequestParam Long attendanceId,
            @RequestAttribute(AuthInterceptor.CURRENT_USER_ATTRIBUTE) CurrentUser currentUser) {
        attendanceService.checkOut(attendanceId, currentUser.id());
        return ApiResponse.success(); // ✅ 现在可以正常调用了
    }

    @PostMapping("/leave")
    @RequirePermission("student:workstudy:view")
    public ApiResponse<Long> applyLeave(@RequestParam Long hireId,
                                   @RequestAttribute(AuthInterceptor.CURRENT_USER_ATTRIBUTE) CurrentUser currentUser,
                                   @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate leaveDate,
                                   @RequestParam Integer leaveType,
                                   @RequestParam String reason) {
        Long attendanceId = attendanceService.applyLeave(hireId, currentUser.id(), leaveDate, leaveType, reason);
        return ApiResponse.success(attendanceId);
    }

    @PostMapping("/repair")
    @RequirePermission("student:workstudy:view")
    public ApiResponse<Long> applyRepair(@RequestParam Long hireId,
                                    @RequestAttribute(AuthInterceptor.CURRENT_USER_ATTRIBUTE) CurrentUser currentUser,
                                    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate attendanceDate,
                                    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime checkInTime,
                                    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime checkOutTime,
                                    @RequestParam String reason) {
        Long attendanceId = attendanceService.applyRepair(hireId, currentUser.id(), attendanceDate, checkInTime, checkOutTime, reason);
        return ApiResponse.success(attendanceId);
    }

    @PostMapping("/approve")
    @RequirePermission("school:workstudy:edit")
    public ApiResponse<Void> approve(@RequestParam Long attendanceId,
                                @RequestAttribute(AuthInterceptor.CURRENT_USER_ATTRIBUTE) CurrentUser currentUser,
                                @RequestParam Boolean approved) {
        attendanceService.approveAttendance(attendanceId, currentUser.id(), approved);
        return ApiResponse.success(); // ✅ 现在可以正常调用了
    }
}
