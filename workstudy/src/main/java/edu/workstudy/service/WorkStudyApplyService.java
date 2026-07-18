package edu.workstudy.service; // 补上包声明，这是Java类的"身份证"，必须在文件最开头

import com.baomidou.mybatisplus.extension.service.IService;
import edu.workstudy.entity.WorkStudyApply; // 导入实体类，告诉编译器WorkStudyApply在哪

public interface WorkStudyApplyService extends IService<WorkStudyApply> {
    /**
     * 学生报名勤工助学岗位
     * @param positionId 岗位ID
     * @param studentId 学生ID
     * @return 报名编号
     */
    String apply(Long positionId, Long studentId);
}