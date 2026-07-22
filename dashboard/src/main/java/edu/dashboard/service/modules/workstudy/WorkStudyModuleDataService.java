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

        String fromJoin = """
        FROM gc_work_study_position p
        LEFT JOIN gc_work_study_hire h ON p.id = h.position_id AND h.is_deleted = 0
        LEFT JOIN gc_student s ON h.student_id = s.id AND s.is_deleted = 0
        LEFT JOIN gc_college c ON s.college_id = c.id AND c.is_deleted = 0
        LEFT JOIN (
            SELECT hire_id, SUM(work_hours) AS total_hours
            FROM gc_work_study_attendance
            WHERE is_deleted = 0
            GROUP BY hire_id
        ) att ON h.id = att.hire_id
        LEFT JOIN (
            SELECT hire_id, SUM(final_amount) AS total_amount
            FROM gc_work_study_salary
            WHERE is_deleted = 0
            GROUP BY hire_id
        ) sal ON h.id = sal.hire_id
        """;

        StringBuilder where = new StringBuilder("WHERE p.is_deleted = 0");

        if (filters != null && filters.containsKey("collegeId")) {
            where.append(" AND c.id = ").append(filters.get("collegeId"));
        }
        if (filters != null && filters.containsKey("batchId")) {
            where.append(" AND p.batch_id = ").append(filters.get("batchId"));
        }
        if (filters != null && filters.containsKey("status")) {
            where.append(" AND p.status = ").append(filters.get("status"));
        }

        String orderBy = " ORDER BY p.id ASC, s.name ASC";

        String finalSql = "SELECT " + columns + " " + fromJoin + " " + where + orderBy;

        System.out.println("【Final SQL】" + finalSql);
        return dynamicQueryMapper.execute(finalSql);
    }
}