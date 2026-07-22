package edu.greenchannel.dashboard.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface ReportMapper {
    /**
     * FR-3.16-001: 动态字段查询
     * @param columns 经过白名单验证的列名 (带表别名)
     */
    List<Map<String, Object>> selectDynamicReport(@Param("columns") List<String> columns, @Param("filters") Map<String, Object> filters);
}