package edu.greenchannel.workstudy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.greenchannel.workstudy.entity.WorkStudyAttendance;
import edu.greenchannel.workstudy.entity.WorkStudyHire;
import edu.greenchannel.workstudy.entity.WorkStudyPosition;
import edu.greenchannel.workstudy.entity.WorkStudySalary;
import edu.greenchannel.workstudy.mapper.WorkStudyAttendanceMapper;
import edu.greenchannel.workstudy.mapper.WorkStudyHireMapper;
import edu.greenchannel.workstudy.mapper.WorkStudyPositionMapper;
import edu.greenchannel.workstudy.mapper.WorkStudySalaryMapper;
import edu.greenchannel.workstudy.service.WorkStudySalaryService;
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
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();

        for (WorkStudyHire hire : hires) {

            long existingCount = baseMapper.selectCount(
                    new LambdaQueryWrapper<WorkStudySalary>()
                            .eq(WorkStudySalary::getHireId, hire.getId())
                            .eq(WorkStudySalary::getSalaryYear, yearMonth.getYear())
                            .eq(WorkStudySalary::getSalaryMonth, yearMonth.getMonthValue())
            );
            if (existingCount > 0) continue;

            // 考勤查询
            List<WorkStudyAttendance> attendances = attendanceMapper.selectList(
                    new LambdaQueryWrapper<WorkStudyAttendance>()
                            .eq(WorkStudyAttendance::getHireId, hire.getId())
                            .between(WorkStudyAttendance::getAttendanceDate, start, end)
                            // 包含：正常、迟到、迟到+早退、早退
                            .in(WorkStudyAttendance::getStatus, 1, 2, 3, 5)
                            // 只统计已审批通过的
                            .eq(WorkStudyAttendance::getApprovalStatus, 2)
            );

            BigDecimal totalHours = attendances.stream()
                    .map(a -> a.getWorkHours() != null ? a.getWorkHours() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            long workDays = attendances.size();

            WorkStudyPosition position = positionMapper.selectById(hire.getPositionId());
            if (position == null) continue;

            // 使用录用快照，而不是岗位当前费率
            BigDecimal salaryRate = hire.getSalaryRate();
            if (salaryRate == null) {
                salaryRate = position.getSalaryRate(); // 兜底（老数据）
            }

            BigDecimal calculatedAmount = totalHours.multiply(salaryRate);

            WorkStudySalary salary = new WorkStudySalary();
            salary.setHireId(hire.getId());
            salary.setStudentId(hire.getStudentId());
            salary.setPositionId(hire.getPositionId());
            salary.setSalaryYear(yearMonth.getYear());
            salary.setSalaryMonth(yearMonth.getMonthValue());
            salary.setTotalWorkHours(totalHours);
            salary.setTotalWorkDays((int) workDays);
            salary.setSalaryRate(salaryRate); // 存快照
            salary.setCalculatedAmount(calculatedAmount);
            salary.setStatus(1);

            save(salary);
            count++;
        }
        return count;
    }
}