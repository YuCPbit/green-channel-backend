package edu.dashboard.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Mapper
public interface WorkStudyMapper {
    /**
     * 岗位统计
     */
    List<Map<String, Object>> selectPositionStats();

    /**
     * 按学院统计学生
     */
    List<Map<String, Object>> selectStudentByCollegeStats();

    /**
     * 按贫困等级统计学生
     */
    List<Map<String, Object>> selectStudentByPovertyStats();

    /**
     * 月度薪酬统计
     */
    List<Map<String, Object>> selectSalaryMonthlyStats();

    /**
     * 学期薪酬统计
     */
    List<Map<String, Object>> selectSalaryTermStats();

    /**
     * 岗位总数
     */
    Long countPositions();

    /**
     * 在岗人数
     */
    Long countHired();

    /**
     * 月度薪酬总额
     */
    BigDecimal sumMonthlySalary();
}