package edu.greenchannel.workstudy.service;

import com.baomidou.mybatisplus.spring.service.IService;
import edu.greenchannel.workstudy.entity.WorkStudyApply;
import java.util.List;

public interface WorkStudyApplyService extends IService<WorkStudyApply> {
    /**
     * 学生报名岗位
     */
    Long applyForPosition(Long positionId, Long studentId, WorkStudyApply applyInfo);

    /**
     * 录入面试结果（简化版，只更新状态）
     */
    void recordInterviewResult(Long applyId, Integer interviewStatus);

    /**
     * 获取学生的申请列表
     */
    List<WorkStudyApply> getStudentApplications(Long studentId);

    /**
     * 获取岗位的申请列表
     */
    List<WorkStudyApply> getPositionApplications(Long positionId);

    /**
     * 辅导员填写推荐意见
     */
    void addTutorRecommendation(Long applyId, String recommendation);

    /**
     * 检查学生在该批次的报名数量
     */
    int countStudentApplicationsInBatch(Long studentId, Long batchId);
}