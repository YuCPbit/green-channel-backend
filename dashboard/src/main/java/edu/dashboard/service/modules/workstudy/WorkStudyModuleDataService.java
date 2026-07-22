package edu.dashboard.service.modules.workstudy;

import edu.dashboard.common.FieldWhiteList;
import edu.dashboard.mapper.DynamicQueryMapper;
import edu.dashboard.service.ModuleDataService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class WorkStudyModuleDataService implements ModuleDataService {

    private final DynamicQueryMapper dynamicQueryMapper;
    private final FieldWhiteList fieldWhiteList;

    public WorkStudyModuleDataService(DynamicQueryMapper dynamicQueryMapper, FieldWhiteList fieldWhiteList) {
        this.dynamicQueryMapper = dynamicQueryMapper;
        this.fieldWhiteList = fieldWhiteList;
    }

    @Override
    public String getModuleCode() {
        return "workstudy";
    }

    @Override
    public Object getCoreStats() {
        String sql = """
        SELECT COUNT(*) total
        FROM gc_work_study_position
        WHERE is_deleted = 0
        """;
        return dynamicQueryMapper.execute(sql).get(0);
    }

    @Override
    public Object getChartData(String chartType) {
        // 图表数据也是固定的
        return List.of();
    }

    @Override
    public List<Map<String, Object>> getReportData(String reportType, Map<String, Object> filters) {
        String columns = fieldWhiteList.getSafeColumns("workstudy_" + reportType, null);

        String fromJoin;
        String whereClause = buildCommonWhereClause(filters);
        String groupBy = "";
        String orderBy = "";

        // 根据报表类型分发
        switch (reportType) {
            case "position-stat":
                fromJoin = buildPositionStatFromJoin();
                groupBy = " GROUP BY p.department_name ";
                orderBy = " ORDER BY p.department_name ";
                break;
            case "student-stat":
                fromJoin = buildStudentStatFromJoin();
                groupBy = " GROUP BY c.college_name, s.poverty_level ";
                orderBy = " ORDER BY c.college_name, s.poverty_level ";
                break;
            case "salary-stat":
                fromJoin = buildSalaryStatFromJoin();
                groupBy = " GROUP BY sal.salary_year, sal.salary_month ";
                orderBy = " ORDER BY sal.salary_year DESC, sal.salary_month DESC ";
                break;
            case "position-detail":
                // 保留你之前的详细报表逻辑
                return getPositionDetailReport(filters);
            default:
                throw new IllegalArgumentException("未知的报表类型: " + reportType);
        }

        String finalSql = "SELECT " + columns + " " + fromJoin + " " + whereClause + groupBy + orderBy;

        System.out.println("【Final SQL】" + finalSql);
        return dynamicQueryMapper.execute(finalSql);
    }

    /**
     * 构建岗位统计的 FROM 和 JOIN
     */
    private String buildPositionStatFromJoin() {
        return """
        FROM gc_work_study_position p
        LEFT JOIN gc_work_study_apply app ON p.id = app.position_id AND app.is_deleted = 0
        LEFT JOIN gc_work_study_hire h ON p.id = h.position_id AND h.is_deleted = 0
        """;
    }

    /**
     * 构建学生统计的 FROM 和 JOIN
     */
    private String buildStudentStatFromJoin() {
        return """
        FROM gc_work_study_hire h
        INNER JOIN gc_student s ON h.student_id = s.id AND s.is_deleted = 0
        INNER JOIN gc_college c ON s.college_id = c.id AND c.is_deleted = 0
        """;
    }

    /**
     * 构建薪酬统计的 FROM 和 JOIN
     */
    private String buildSalaryStatFromJoin() {
        return """
        FROM gc_work_study_salary sal
        WHERE sal.is_deleted = 0 AND sal.status = 3
        """;
    }

    /**
     * 构建公共的 WHERE 条件
     */
    private String buildCommonWhereClause(Map<String, Object> filters) {
        StringBuilder where = new StringBuilder("WHERE 1=1 ");

        // 默认只查未删除的数据
        where.append("AND p.is_deleted = 0 ");

        if (filters != null) {
            // 按批次筛选（适用于岗位相关统计）
            if (filters.containsKey("batchId")) {
                where.append("AND p.batch_id = ").append(filters.get("batchId")).append(" ");
            }
            // 按年份筛选（适用于薪酬统计）
            if (filters.containsKey("year")) {
                where.append("AND sal.salary_year = ").append(filters.get("year")).append(" ");
            }
            // 按月份筛选
            if (filters.containsKey("month")) {
                where.append("AND sal.salary_month = ").append(filters.get("month")).append(" ");
            }
            // 按学院筛选（适用于学生统计）
            if (filters.containsKey("collegeId")) {
                where.append("AND s.college_id = ").append(filters.get("collegeId")).append(" ");
            }
        }
        return where.toString();
    }

    /**
     * 保留原有的岗位详情报表（单表查询，用于导出明细）
     */
    private List<Map<String, Object>> getPositionDetailReport(Map<String, Object> filters) {
        String columns = fieldWhiteList.getSafeColumns("workstudy_position-detail", null);
        String fromJoin = "FROM gc_work_study_position p";
        String where = "WHERE p.is_deleted = 0";

        if (filters != null && filters.containsKey("status")) {
            where += " AND p.status = " + filters.get("status");
        }

        String finalSql = "SELECT " + columns + " " + fromJoin + " " + where + " ORDER BY p.id ASC";
        return dynamicQueryMapper.execute(finalSql);
    }
}