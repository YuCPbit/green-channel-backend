package edu.greenchannel.workstudy.service;

import com.baomidou.mybatisplus.spring.service.IService;
import edu.greenchannel.workstudy.dto.ActiveHireVO;
import edu.greenchannel.workstudy.dto.PageResult;
import edu.greenchannel.workstudy.dto.WorkStudyEvaluationVO;
import edu.greenchannel.workstudy.entity.WorkStudyEvaluation;

import java.util.List;

public interface WorkStudyEvaluationService extends IService<WorkStudyEvaluation> {

    /**
     * 提交月度评价
     */
    void submitEvaluation(WorkStudyEvaluation evaluation);

    /**
     * 分页查询评价列表（支持筛选）
     */
    PageResult<WorkStudyEvaluationVO> listEvaluations(int page, int size, Long hireId, Long studentId, Integer evalYear, Integer evalMonth);

    /**
     * 查询评价详情（含关联信息）
     */
    WorkStudyEvaluationVO getDetail(Long id);

    /**
     * 更新评价（仅允许修改评分和评语）
     */
    void updateEvaluation(Long id, Integer score, String comment, Long operatorId);

    /**
     * 删除评价（软删除）
     */
    void deleteEvaluation(Long id);

    /**
     * 学生查看本人评价
     */
    PageResult<WorkStudyEvaluationVO> getMyEvaluations(int page, int size, Long studentId, Integer evalYear, Integer evalMonth);

    /**
     * 获取在岗录用列表（支持姓名/学号搜索）
     */
    List<ActiveHireVO> getActiveHires(String keyword);
}
