package edu.greenchannel.workstudy.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 勤工助学评价视图对象 — 关联学生姓名、学号、岗位名称、用工部门
 */
@Data
public class WorkStudyEvaluationVO {
    private Long id;
    private Long hireId;
    private Long studentId;
    private String studentNo;
    private String studentName;
    private String positionName;
    private String departmentName;
    private Integer evalYear;
    private Integer evalMonth;
    private Integer score;
    private String comment;
    private Long evaluatorId;
    private LocalDateTime evalTime;
    private LocalDateTime createTime;
}
