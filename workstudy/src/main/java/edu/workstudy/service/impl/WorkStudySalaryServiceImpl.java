package edu.workstudy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.workstudy.entity.WorkStudyAttendance;
import edu.workstudy.entity.WorkStudyHire;
import edu.workstudy.entity.WorkStudyPosition;
import edu.workstudy.entity.WorkStudySalary;
import edu.workstudy.mapper.WorkStudyAttendanceMapper;
import edu.workstudy.mapper.WorkStudyHireMapper;
import edu.workstudy.mapper.WorkStudyPositionMapper;
import edu.workstudy.mapper.WorkStudySalaryMapper;
import edu.workstudy.service.WorkStudySalaryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
public class WorkStudySalaryServiceImpl
        extends ServiceImpl<WorkStudySalaryMapper, WorkStudySalary>
        implements WorkStudySalaryService {

    private final WorkStudyAttendanceMapper attendanceMapper;
    private final WorkStudyHireMapper hireMapper;
    private final WorkStudyPositionMapper positionMapper;

    public WorkStudySalaryServiceImpl(WorkStudyAttendanceMapper attendanceMapper,
                                      WorkStudyHireMapper hireMapper,
                                      WorkStudyPositionMapper positionMapper) {
        this.attendanceMapper = attendanceMapper;
        this.hireMapper = hireMapper;
        this.positionMapper = positionMapper;
    }

    @Override
    @Transactional
    public int calculateMonthlySalary(YearMonth yearMonth) {
        List<WorkStudyHire> hires = hireMapper.selectList(
                new LambdaQueryWrapper<WorkStudyHire>()
                        .eq(WorkStudyHire::getHireStatus, 1)
        );

        int count = 0;
        for (WorkStudyHire hire : hires) {

            long existingCount = baseMapper.selectCount(
                    new LambdaQueryWrapper<WorkStudySalary>()
                            .eq(WorkStudySalary::getHireId, hire.getId())
                            .eq(WorkStudySalary::getSalaryYear, yearMonth.getYear())
                            .eq(WorkStudySalary::getSalaryMonth, yearMonth.getMonthValue())
            );
            if (existingCount > 0) {
                continue; // 已核算过，跳过
            }

            LocalDate start = yearMonth.atDay(1);
            LocalDate end = yearMonth.atEndOfMonth();

            // 计算总工时
            BigDecimal totalHours = attendanceMapper.selectList(
                            new LambdaQueryWrapper<WorkStudyAttendance>()
                                    .eq(WorkStudyAttendance::getHireId, hire.getId())
                                    .between(WorkStudyAttendance::getAttendanceDate, start, end)
                                    .eq(WorkStudyAttendance::getStatus, 1)
                    ).stream()
                    .map(a -> a.getWorkHours() != null ? a.getWorkHours() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            long workDays = attendanceMapper.selectCount(
                    new LambdaQueryWrapper<WorkStudyAttendance>()
                            .eq(WorkStudyAttendance::getHireId, hire.getId())
                            .between(WorkStudyAttendance::getAttendanceDate, start, end)
                            .eq(WorkStudyAttendance::getStatus, 1)
            );

            WorkStudyPosition position = positionMapper.selectById(hire.getPositionId());
            if (position == null) continue;

            BigDecimal salaryRate = position.getSalaryRate();
            BigDecimal calculatedAmount = totalHours.multiply(salaryRate);

            WorkStudySalary salary = new WorkStudySalary();
            salary.setHireId(hire.getId());
            salary.setStudentId(hire.getStudentId());
            salary.setPositionId(hire.getPositionId());
            salary.setSalaryYear(yearMonth.getYear());
            salary.setSalaryMonth(yearMonth.getMonthValue());
            salary.setTotalWorkHours(totalHours);
            salary.setTotalWorkDays((int) workDays);
            salary.setSalaryRate(salaryRate);
            salary.setCalculatedAmount(calculatedAmount);
            salary.setStatus(1);

            save(salary);
            count++;
        }
        return count;
    }
}