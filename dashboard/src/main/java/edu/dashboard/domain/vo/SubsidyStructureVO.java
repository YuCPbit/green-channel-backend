package edu.dashboard.domain.vo;

import lombok.Data;

@Data
public class SubsidyStructureVO {
    private String name; // 补助类型名称
    private Integer value; // 金额或人数
}