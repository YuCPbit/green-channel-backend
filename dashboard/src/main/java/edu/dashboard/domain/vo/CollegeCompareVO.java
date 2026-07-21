package edu.dashboard.domain.vo;

import lombok.Data;

@Data
public class CollegeCompareVO {
    private String collegeName;    // 学院名称
    private Long totalCount;      // 申请总数
    private Long approvedCount;   // 通过总数
    private Double rate;          // 通过率
}