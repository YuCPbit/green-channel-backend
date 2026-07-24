package edu.greenchannel.workstudy.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 在岗录用记录视图 — 用于评价表单下拉选择
 */
@Data
public class ActiveHireVO {
    private Long hireId;
    private Long studentId;
    private String studentNo;
    private String studentName;
    private Long positionId;
    private String positionName;
    private String departmentName;
    private BigDecimal salaryRate;
    private LocalDate hireDate;
}
