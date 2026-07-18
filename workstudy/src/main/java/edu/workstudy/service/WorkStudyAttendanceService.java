package edu.workstudy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.workstudy.entity.WorkStudyAttendance;
import java.math.BigDecimal;

public interface WorkStudyAttendanceService extends IService<WorkStudyAttendance> {
    Long checkIn(Long hireId, Long studentId, String location);
    void checkOut(Long attendanceId);
}