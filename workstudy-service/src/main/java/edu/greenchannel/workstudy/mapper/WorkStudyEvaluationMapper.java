package edu.greenchannel.workstudy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import edu.greenchannel.workstudy.dto.ActiveHireVO;
import edu.greenchannel.workstudy.dto.WorkStudyEvaluationVO;
import edu.greenchannel.workstudy.entity.WorkStudyEvaluation;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface WorkStudyEvaluationMapper extends BaseMapper<WorkStudyEvaluation> {

    /**
     * 分页查询评价列表 — JOIN 学生、录用、岗位表
     */
    @Select("<script>" +
            "SELECT e.id, e.hire_id AS hireId, e.student_id AS studentId, " +
            "s.student_no AS studentNo, s.name AS studentName, " +
            "p.position_name AS positionName, p.department_name AS departmentName, " +
            "e.eval_year AS evalYear, e.eval_month AS evalMonth, " +
            "e.score, e.comment, e.evaluator_id AS evaluatorId, " +
            "e.eval_time AS evalTime, e.create_time AS createTime " +
            "FROM gc_work_study_evaluation e " +
            "LEFT JOIN gc_student s ON e.student_id = s.id " +
            "LEFT JOIN gc_work_study_hire h ON e.hire_id = h.id " +
            "LEFT JOIN gc_work_study_position p ON h.position_id = p.id " +
            "WHERE e.is_deleted = 0 " +
            "<if test='ew != null and ew.sqlSegment != null and ew.sqlSegment != \"\"'>AND ${ew.sqlSegment}</if>" +
            " ORDER BY s.name ASC, e.eval_year DESC, e.eval_month DESC" +
            "</script>")
    IPage<WorkStudyEvaluationVO> selectEvaluationPage(Page<?> page, @Param("ew") com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<?> wrapper);

    /**
     * 单条评价详情 — 含关联信息
     */
    @Select("SELECT e.id, e.hire_id AS hireId, e.student_id AS studentId, " +
            "s.student_no AS studentNo, s.name AS studentName, " +
            "p.position_name AS positionName, p.department_name AS departmentName, " +
            "e.eval_year AS evalYear, e.eval_month AS evalMonth, " +
            "e.score, e.comment, e.evaluator_id AS evaluatorId, " +
            "e.eval_time AS evalTime, e.create_time AS createTime " +
            "FROM gc_work_study_evaluation e " +
            "LEFT JOIN gc_student s ON e.student_id = s.id " +
            "LEFT JOIN gc_work_study_hire h ON e.hire_id = h.id " +
            "LEFT JOIN gc_work_study_position p ON h.position_id = p.id " +
            "WHERE e.id = #{id} AND e.is_deleted = 0")
    WorkStudyEvaluationVO selectEvaluationById(@Param("id") Long id);

    /**
     * 查询在岗录用记录 — 支持姓名/学号搜索
     */
    @Select("<script>" +
            "SELECT h.id AS hireId, h.student_id AS studentId, " +
            "s.student_no AS studentNo, s.name AS studentName, " +
            "h.position_id AS positionId, " +
            "p.position_name AS positionName, p.department_name AS departmentName, " +
            "p.salary_rate AS salaryRate, h.hire_date AS hireDate " +
            "FROM gc_work_study_hire h " +
            "LEFT JOIN gc_student s ON h.student_id = s.id " +
            "LEFT JOIN gc_work_study_position p ON h.position_id = p.id " +
            "WHERE h.hire_status = 1 AND h.is_deleted = 0 " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND (s.name LIKE CONCAT('%', #{keyword}, '%') OR s.student_no LIKE CONCAT('%', #{keyword}, '%')) " +
            "</if>" +
            "ORDER BY h.hire_date DESC" +
            "</script>")
    List<ActiveHireVO> selectActiveHires(@Param("keyword") String keyword);

    /**
     * 根据 userId 查询 studentId
     */
    @Select("SELECT id FROM gc_student WHERE user_id = #{userId} AND is_deleted = 0 LIMIT 1")
    Long findStudentIdByUserId(@Param("userId") Long userId);
}
