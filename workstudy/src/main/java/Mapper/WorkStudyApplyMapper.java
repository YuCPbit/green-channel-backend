@Select("""
    SELECT COUNT(*) FROM gc_work_study_apply
    WHERE position_id = #{pid} AND student_id = #{sid} AND is_deleted = 0
""")
int countByPositionAndStudent(@Param("pid") Long pid, @Param("sid") Long sid);