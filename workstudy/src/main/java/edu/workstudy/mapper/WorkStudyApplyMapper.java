package edu.workstudy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import edu.workstudy.entity.WorkStudyApply;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface WorkStudyApplyMapper extends BaseMapper<WorkStudyApply> {

    @Select("""
        SELECT COUNT(*) FROM gc_work_study_apply 
        WHERE position_id = #{positionId} 
        AND student_id = #{studentId} 
        AND is_deleted = 0
        """)
    int countByPositionAndStudent(@Param("positionId") Long positionId, @Param("studentId") Long studentId);
}