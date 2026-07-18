package edu.workstudy.controller;

import edu.workstudy.common.Result;
import edu.workstudy.service.WorkStudyAttendanceService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/attendance")
public class WorkStudyAttendanceController {

    private final WorkStudyAttendanceService attendanceService;

    public WorkStudyAttendanceController(WorkStudyAttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @PostMapping("/check-in")
    public Result<?> checkIn(@RequestParam Long hireId, @RequestParam Long studentId, @RequestParam String location) {
        Long attendanceId = attendanceService.checkIn(hireId, studentId, location);
        return Result.success(attendanceId);
    }

    @PostMapping("/check-out")
    public Result<?> checkOut(@RequestParam Long attendanceId) {
        attendanceService.checkOut(attendanceId);
        return Result.success("签退成功");
    }
}