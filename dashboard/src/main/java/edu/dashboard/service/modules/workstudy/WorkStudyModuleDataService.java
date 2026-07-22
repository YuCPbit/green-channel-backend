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

// 1. 只查单表，确保肯定有数据且语法简单

        String columns = "p.id, p.position_name, p.department_name";

        String fromJoin = "FROM gc_work_study_position p";

        String where = "WHERE p.is_deleted = 0";

        String finalSql = "SELECT " + columns + " " + fromJoin + " " + where;

// 打印出来确认一下
        System.out.println("【TEST SQL】" + finalSql);

        return dynamicQueryMapper.execute(finalSql);

    }
}