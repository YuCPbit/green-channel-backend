package edu.greenchannel.workstudy.service;

import com.baomidou.mybatisplus.spring.service.IService;
import edu.greenchannel.workstudy.entity.WorkStudyEvaluation;

public interface WorkStudyEvaluationService extends IService<WorkStudyEvaluation> {
    void submitEvaluation(WorkStudyEvaluation evaluation);
}