package edu.dashboard.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;
import java.util.Map;

@Mapper
public interface DynamicQueryMapper {

    /**
     * 执行动态SQL查询
     * @param sql 由 ModuleDataService 拼装完成的 SQL
     * @return 结果集
     */
    @Select("${sql}")
    List<Map<String, Object>> execute(@Param("sql") String sql);
}