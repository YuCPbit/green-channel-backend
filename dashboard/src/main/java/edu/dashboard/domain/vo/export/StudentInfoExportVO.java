package edu.dashboard.domain.vo.export;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StudentInfoExportVO {
    @ExcelProperty("学号")
    @ColumnWidth(15)
    private String studentNo;

    @ExcelProperty("姓名")
    @ColumnWidth(10)
    private String name;

    @ExcelProperty("性别")
    @ColumnWidth(8)
    private String gender;

    @ExcelProperty("民族")
    @ColumnWidth(10)
    private String nation;

    @ExcelProperty("学院")
    @ColumnWidth(20)
    private String collegeName;

    @ExcelProperty("专业")
    @ColumnWidth(20)
    private String majorName;

    @ExcelProperty("班级")
    @ColumnWidth(15)
    private String className;

    @ExcelProperty("贫困等级")
    @ColumnWidth(12)
    private String povertyLevel;

    @ExcelProperty("联系电话")
    @ColumnWidth(15)
    private String phone;

    @ExcelProperty("申请金额")
    @ColumnWidth(12)
    private Double applyAmount;

    @ExcelProperty("批准金额")
    @ColumnWidth(12)
    private Double approvedAmount;

    @ExcelProperty("申请状态")
    @ColumnWidth(12)
    private String applyStatus;

    @ExcelProperty("申请时间")
    @ColumnWidth(20)
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    private LocalDateTime applyTime;
}