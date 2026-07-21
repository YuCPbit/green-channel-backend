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
        // 核心指标通常是固定的，简单处理
        String sql = "SELECT COUNT(*) total FROM work_study_positions";
        return dynamicQueryMapper.execute(sql).get(0);
    }

    @Override
    public Object getChartData(String chartType) {
        // 图表数据也是固定的
        return List.of();
    }

    @Override
    public List<Map<String, Object>> getReportData(String reportType, Map<String, Object> filters) {
        // 1. 获取安全的列名 (SELECT 部分)
        String columns = fieldWhiteList.getSafeColumns("workstudy_" + reportType, null);

        // 2. 拼装 FROM 和 JOIN (模块私有逻辑)
        String fromJoin = "FROM work_study_students wss " +
                "LEFT JOIN student s ON wss.stu_id = s.id " +
                "LEFT JOIN department d ON wss.dept_id = d.id";

        // 3. 拼装 WHERE (根据 filters)
        String where = "WHERE 1=1";
        // if (filters.containsKey("month")) where += " AND month = " + filters.get("month");

        // 4. 生成最终SQL
        String finalSql = "SELECT " + columns + " " + fromJoin + " " + where;

        // 5. 执行
        return dynamicQueryMapper.execute(finalSql);
    }
}