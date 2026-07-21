package edu.dashboard.service.modules.base;

import edu.dashboard.common.FieldWhiteList;
import edu.dashboard.domain.vo.DashboardStatsVO;
import edu.dashboard.mapper.DynamicQueryMapper;
import edu.dashboard.service.ModuleDataService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class BaseModuleDataService implements ModuleDataService {

    private final DynamicQueryMapper dynamicQueryMapper;
    private final FieldWhiteList fieldWhiteList;

    public BaseModuleDataService(DynamicQueryMapper dynamicQueryMapper, FieldWhiteList fieldWhiteList) {
        this.dynamicQueryMapper = dynamicQueryMapper;
        this.fieldWhiteList = fieldWhiteList;
    }

    @Override
    public String getModuleCode() {
        return "base";
    }

    @Override
    public Object getCoreStats() {
        DashboardStatsVO vo = new DashboardStatsVO();

        // 获取基础统计数据
        String sql = "SELECT " +
                "COUNT(DISTINCT s.id) as totalApplicants, " +
                "SUM(CASE WHEN DATE(s.create_time) = CURDATE() THEN 1 ELSE 0 END) as todayNewApplicants, " +
                "AVG(CASE WHEN sa.status = 'APPROVED' THEN 100.0 ELSE 0 END) as approvalRate, " +
                "SUM(sa.approved_amount) as totalSubsidyAmount, " +
                "COUNT(DISTINCT CASE WHEN gp.id IS NOT NULL THEN s.id END) as giftPackCount, " +
                "NOW() as updateTime " +
                "FROM gc_student s " +
                "LEFT JOIN gc_subsidy_apply sa ON s.id = sa.student_id " +
                "LEFT JOIN gc_gift_pack gp ON s.id = gp.student_id";

        Map<String, Object> result = dynamicQueryMapper.execute(sql).get(0);

        vo.setTotalApplicants(Long.valueOf(result.get("totalApplicants").toString()));
        vo.setTodayNewApplicants(Long.valueOf(result.get("todayNewApplicants").toString()));
        vo.setApprovalRate(Double.valueOf(result.get("approvalRate").toString()));
        vo.setTotalSubsidyAmount(new java.math.BigDecimal(result.get("totalSubsidyAmount").toString()));
        vo.setGiftPackCount(Long.valueOf(result.get("giftPackCount").toString()));
        vo.setUpdateTime(LocalDateTime.parse(result.get("updateTime").toString()));

        return vo;
    }

    @Override
    public Object getChartData(String chartType) {
        return switch (chartType) {
            case "college-compare" -> getCollegeCompareData();
            case "heatmap" -> getHeatmapData();
            case "subsidy-structure" -> getSubsidyStructureData();
            default -> Collections.emptyList();
        };
    }

    @Override
    public List<Map<String, Object>> getReportData(String reportType, Map<String, Object> filters) {
        String columns = fieldWhiteList.getSafeColumns("base_" + reportType, null);
        String where = buildWhereClause(filters);

        String sql = "SELECT " + columns +
                " FROM gc_student s " +
                "LEFT JOIN gc_college c ON s.college_id = c.id " +
                "LEFT JOIN gc_subsidy_apply sa ON s.id = sa.student_id " +
                where;

        return dynamicQueryMapper.execute(sql);
    }

    private List<Map<String, Object>> getCollegeCompareData() {
        String sql = "SELECT c.college_name as collegeName, " +
                "COUNT(s.id) as totalCount, " +
                "SUM(CASE WHEN sa.status = 'APPROVED' THEN 1 ELSE 0 END) as approvedCount, " +
                "ROUND(AVG(CASE WHEN sa.status = 'APPROVED' THEN 100.0 ELSE 0 END), 2) as rate " +
                "FROM gc_student s " +
                "LEFT JOIN gc_college c ON s.college_id = c.id " +
                "LEFT JOIN gc_subsidy_apply sa ON s.id = sa.student_id " +
                "GROUP BY c.id, c.college_name";
        return dynamicQueryMapper.execute(sql);
    }

    private List<Map<String, Object>> getHeatmapData() {
        String sql = "SELECT s.origin_province as name, COUNT(s.id) as value " +
                "FROM gc_student s " +
                "WHERE s.origin_province IS NOT NULL " +
                "GROUP BY s.origin_province";
        return dynamicQueryMapper.execute(sql);
    }

    private List<Map<String, Object>> getSubsidyStructureData() {
        String sql = "SELECT sa.subsidy_type as name, SUM(sa.approved_amount) as value " +
                "FROM gc_subsidy_apply sa " +
                "WHERE sa.status = 'APPROVED' " +
                "GROUP BY sa.subsidy_type";
        return dynamicQueryMapper.execute(sql);
    }

    private String buildWhereClause(Map<String, Object> filters) {
        if (filters == null || filters.isEmpty()) {
            return "";
        }

        StringBuilder where = new StringBuilder(" WHERE 1=1 ");

        filters.forEach((key, value) -> {
            if (value != null && !value.toString().isEmpty()) {
                where.append(" AND ").append(key).append(" = '").append(value).append("'");
            }
        });

        return where.toString();
    }
}