package edu.dashboard.domain.vo.export;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class WorkStudyExportVO {
    @ExcelProperty("用工部门")
    @ColumnWidth(20)
    private String department;

    @ExcelProperty("岗位名称")
    @ColumnWidth(25)
    private String positionName;

    @ExcelProperty("招聘人数")
    @ColumnWidth(12)
    private Integer recruitCount;

    @ExcelProperty("报名人次")
    @ColumnWidth(12)
    private Integer applyCount;

    @ExcelProperty("在岗人数")
    @ColumnWidth(12)
    private Integer hiredCount;

    @ExcelProperty("在岗率(%)")
    @ColumnWidth(12)
    private Double onlineRate;

    @ExcelProperty("学生姓名")
    @ColumnWidth(10)
    private String studentName;

    @ExcelProperty("所在学院")
    @ColumnWidth(20)
    private String collegeName;

    @ExcelProperty("贫困等级")
    @ColumnWidth(12)
    private String povertyLevel;

    @ExcelProperty("总工时")
    @ColumnWidth(10)
    private Double totalWorkHours;

    @ExcelProperty("发放月份")
    @ColumnWidth(12)
    private Integer salaryMonth;

    @ExcelProperty("发放总额(元)")
    @ColumnWidth(15)
    private BigDecimal totalSalary;

    @ExcelProperty("预算执行率(%)")
    @ColumnWidth(15)
    private Double budgetExecRate;
}