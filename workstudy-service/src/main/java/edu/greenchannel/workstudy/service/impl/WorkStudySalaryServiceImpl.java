package edu.greenchannel.workstudy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import edu.greenchannel.common.BusinessException;
import edu.greenchannel.workstudy.entity.WorkStudyAttendance;
import edu.greenchannel.workstudy.entity.WorkStudyHire;
import edu.greenchannel.workstudy.entity.WorkStudyPosition;
import edu.greenchannel.workstudy.entity.WorkStudySalary;
import edu.greenchannel.workstudy.mapper.WorkStudyAttendanceMapper;
import edu.greenchannel.workstudy.mapper.WorkStudyHireMapper;
import edu.greenchannel.workstudy.mapper.WorkStudyPositionMapper;
import edu.greenchannel.workstudy.mapper.WorkStudySalaryMapper;
import edu.greenchannel.workstudy.service.WorkStudySalaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkStudySalaryServiceImpl
        extends ServiceImpl<WorkStudySalaryMapper, WorkStudySalary>
        implements WorkStudySalaryService {

    private final WorkStudyAttendanceMapper attendanceMapper;
    private final WorkStudyHireMapper hireMapper;
    private final WorkStudyPositionMapper positionMapper;

    /**
     * 核算指定年月的薪酬
     * 流程：
     * 1. 查询该月所有在岗的录用记录
     * 2. 统计每个录用的考勤数据（只统计已确认的考勤）
     * 3. 生成薪酬记录（状态：待部门确认）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int calculateMonthlySalary(YearMonth yearMonth) {
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        // 1. 查询所有在岗的录用记录
        List<WorkStudyHire> activeHires = hireMapper.selectList(
                new LambdaQueryWrapper<WorkStudyHire>()
                        .eq(WorkStudyHire::getHireStatus, 1) // 1-在岗
                        .eq(WorkStudyHire::getDeleted, 0)
        );

        int generatedCount = 0;

        for (WorkStudyHire hire : activeHires) {
            // 2. 检查是否已核算过（幂等性控制）
            boolean exists = existsSalaryRecord(hire.getId(), yearMonth);
            if (exists) {
                log.info("薪酬记录已存在，跳过：hireId={}, yearMonth={}",
                        hire.getId(), yearMonth);
                continue;
            }

            // 3. 统计考勤数据（只统计已确认的考勤）
            AttendanceSummary summary = summarizeAttendance(hire.getId(), startDate, endDate);

            // 4. 获取岗位信息和薪酬标准（使用录用时的快照）
            WorkStudyPosition position = positionMapper.selectById(hire.getPositionId());
            if (position == null) {
                log.warn("岗位不存在，跳过：positionId={}", hire.getPositionId());
                continue;
            }

            // 5. 计算薪酬（优先使用录用快照，兜底使用岗位当前费率）
            BigDecimal salaryRate = hire.getSalaryRate() != null ?
                    hire.getSalaryRate() : position.getSalaryRate();

            if (salaryRate == null) {
                log.error("薪酬标准为空，跳过：hireId={}", hire.getId());
                continue;
            }

            BigDecimal calculatedAmount = summary.totalHours()
                    .multiply(salaryRate)
                    .setScale(2, BigDecimal.ROUND_HALF_UP);

            // 6. 生成薪酬记录
            WorkStudySalary salary = new WorkStudySalary();
            salary.setHireId(hire.getId());
            salary.setStudentId(hire.getStudentId());
            salary.setPositionId(hire.getPositionId());
            salary.setSalaryYear(yearMonth.getYear());
            salary.setSalaryMonth(yearMonth.getMonthValue());
            salary.setTotalWorkHours(summary.totalHours());
            salary.setTotalWorkDays(summary.workDays());
            salary.setSalaryRate(salaryRate);
            salary.setCalculatedAmount(calculatedAmount);
            salary.setStatus(1); // 1-待部门确认
            salary.setDeleted(0);
            salary.setCreateTime(LocalDateTime.now());
            salary.setUpdateTime(LocalDateTime.now());

            save(salary);
            generatedCount++;

            log.info("薪酬核算完成：hireId={}, amount={}, hours={}",
                    hire.getId(), calculatedAmount, summary.totalHours());
        }

        return generatedCount;
    }

    /**
     * 部门确认薪酬
     */
    @Transactional(rollbackFor = Exception.class)
    public void confirmByDepartment(Long salaryId, Long deptUserId, BigDecimal confirmedAmount) {
        WorkStudySalary salary = getById(salaryId);
        if (salary == null || salary.getDeleted() == 1) {
            throw new BusinessException(40400, "薪酬记录不存在");
        }

        // 只有待部门确认状态的才能确认
        if (salary.getStatus() != 1) {
            throw new BusinessException(40000, "当前状态不允许部门确认");
        }

        salary.setDeptConfirmId(deptUserId);
        salary.setDeptConfirmTime(LocalDateTime.now());
        salary.setConfirmedAmount(confirmedAmount);
        salary.setStatus(2); // 2-待资助中心审批
        salary.setUpdateTime(LocalDateTime.now());

        updateById(salary);
        log.info("部门确认薪酬：salaryId={}, confirmedAmount={}", salaryId, confirmedAmount);
    }

    /**
     * 资助中心审批薪酬
     */
    @Transactional(rollbackFor = Exception.class)
    public void approveBySchool(Long salaryId, Long schoolUserId, BigDecimal finalAmount) {
        WorkStudySalary salary = getById(salaryId);
        if (salary == null || salary.getDeleted() == 1) {
            throw new BusinessException(40400, "薪酬记录不存在");
        }

        // 只有待资助中心审批状态的才能审批
        if (salary.getStatus() != 2) {
            throw new BusinessException(40000, "当前状态不允许资助中心审批");
        }

        salary.setSchoolApproveId(schoolUserId);
        salary.setSchoolApproveTime(LocalDateTime.now());
        salary.setFinalAmount(finalAmount);
        salary.setStatus(3); // 3-已审批
        salary.setUpdateTime(LocalDateTime.now());

        updateById(salary);
        log.info("资助中心审批薪酬：salaryId={}, finalAmount={}", salaryId, finalAmount);
    }

    /**
     * 标记薪酬已发放
     */
    @Transactional(rollbackFor = Exception.class)
    public void markAsPaid(Long salaryId, Long operatorId) {
        WorkStudySalary salary = getById(salaryId);
        if (salary == null || salary.getDeleted() == 1) {
            throw new BusinessException(40400, "薪酬记录不存在");
        }

        // 只有已审批状态的才能标记为已发放
        if (salary.getStatus() != 3) {
            throw new BusinessException(40000, "当前状态不允许标记为已发放");
        }

        salary.setStatus(4); // 4-已发放
        salary.setUpdateTime(LocalDateTime.now());

        updateById(salary);
        log.info("薪酬已发放：salaryId={}", salaryId);
    }

    // ==================== 私有方法 ====================

    /**
     * 检查薪酬记录是否已存在
     */
    private boolean existsSalaryRecord(Long hireId, YearMonth yearMonth) {
        return lambdaQuery()
                .eq(WorkStudySalary::getHireId, hireId)
                .eq(WorkStudySalary::getSalaryYear, yearMonth.getYear())
                .eq(WorkStudySalary::getSalaryMonth, yearMonth.getMonthValue())
                .eq(WorkStudySalary::getDeleted, 0)
                .exists();
    }

    /**
     * 统计考勤数据（只统计已确认的考勤）
     */
    private AttendanceSummary summarizeAttendance(Long hireId, LocalDate startDate, LocalDate endDate) {
        // 查询已确认的考勤记录
        // 状态：1-正常 7-补打卡已通过
        // 必须有部门确认人（confirmed_by不为空）
        List<WorkStudyAttendance> attendances = attendanceMapper.selectList(
                new LambdaQueryWrapper<WorkStudyAttendance>()
                        .eq(WorkStudyAttendance::getHireId, hireId)
                        .between(WorkStudyAttendance::getAttendanceDate, startDate, endDate)
                        .in(WorkStudyAttendance::getStatus, 1, 7) // 1-正常 7-补打卡已通过
                        .isNotNull(WorkStudyAttendance::getConfirmedBy) // 部门已确认
                        .eq(WorkStudyAttendance::getDeleted, 0)
        );

        BigDecimal totalHours = attendances.stream()
                .map(a -> a.getWorkHours() != null ? a.getWorkHours() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 计算出勤天数（按日期去重）
        long workDays = attendances.stream()
                .map(WorkStudyAttendance::getAttendanceDate)
                .distinct()
                .count();

        return new AttendanceSummary(totalHours, (int) workDays);
    }

    /**
     * 考勤汇总内部类
     */
    private record AttendanceSummary(BigDecimal totalHours, int workDays) {}
}