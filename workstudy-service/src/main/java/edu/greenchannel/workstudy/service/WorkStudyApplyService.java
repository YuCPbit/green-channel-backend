package edu.greenchannel.workstudy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.greenchannel.workstudy.entity.WorkStudyApply;

public interface WorkStudyApplyService extends IService<WorkStudyApply> {
    Long applyForPosition(Long positionId, Long studentId, WorkStudyApply applyInfo);

    void recordInterviewResult(Long applyId, Long interviewerId, Integer interviewStatus, String remark);
}
