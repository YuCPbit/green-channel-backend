package edu.dashboard.mapper;

import edu.dashboard.domain.vo.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface DashboardMapper {

    /**
     * FR-3.14-002: 获取核心指标
     */
    DashboardStatsVO selectCoreStats();

    /**
     * FR-3.14-003: 学院对比
     */
    List<CollegeCompareVO> selectCollegeCompare();

    /**
     * FR-3.14-003: 生源地热力图
     * 注意：这里假设 gc_student 表中有 province 字段，你的表里是 home_address，需要截取或存储省份
     * 此处简化为直接使用 home_address，实际生产环境建议增加 province 字段
     */
    List<HeatmapVO> selectOriginHeatmap();

    /**
     * FR-3.14-003: 补助结构
     */
    List<SubsidyStructureVO> selectSubsidyStructure();

    /**
     * FR-3.14-004: 年度趋势对比
     */
    List<Map<String, Object>> selectYearlyTrend(@Param("currentYear") String currentYear);
}