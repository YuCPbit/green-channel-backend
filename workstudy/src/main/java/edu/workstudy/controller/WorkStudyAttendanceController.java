package edu.workstudy.controller;

import edu.workstudy.common.Result;
import edu.workstudy.service.WorkStudyAttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/attendance")
@RequiredArgsConstructor
public class WorkStudyAttendanceController {

    private final WorkStudyAttendanceService attendanceService;

    @PostMapping("/check-in")
    public Result<Long> checkIn(@RequestParam Long hireId,
                                @RequestParam Long studentId,
                                @RequestParam String location) {
        Long attendanceId = attendanceService.checkIn(hireId, studentId, location);
        return Result.success(attendanceId);
    }

    @PostMapping("/check-out")
    public Result<Void> checkOut(@RequestParam Long attendanceId) {
        attendanceService.checkOut(attendanceId);
        return Result.success(); // ✅ 现在可以正常调用了
    }

    @PostMapping("/leave")
    public Result<Long> applyLeave(@RequestParam Long hireId,
                                   @RequestParam Long studentId,
                                   @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate leaveDate,
                                   @RequestParam Integer leaveType,
                                   @RequestParam String reason) {
        Long attendanceId = attendanceService.applyLeave(hireId, studentId, leaveDate, leaveType, reason);
        return Result.success(attendanceId);
    }

    @PostMapping("/repair")
    public Result<Long> applyRepair(@RequestParam Long hireId,
                                    @RequestParam Long studentId,
                                    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate attendanceDate,
                                    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime checkInTime,
                                    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime checkOutTime,
                                    @RequestParam String reason) {
        Long attendanceId = attendanceService.applyRepair(hireId, studentId, attendanceDate, checkInTime, checkOutTime, reason);
        return Result.success(attendanceId);
    }

    @PostMapping("/approve")
    public Result<Void> approve(@RequestParam Long attendanceId,
                                @RequestParam Long approverId,
                                @RequestParam Boolean approved) {
        attendanceService.approveAttendance(attendanceId, approverId, approved);
        return Result.success(); // ✅ 现在可以正常调用了
    }
}