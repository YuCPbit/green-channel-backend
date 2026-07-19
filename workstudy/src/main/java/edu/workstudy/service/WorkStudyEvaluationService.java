package edu.workstudy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.workstudy.entity.WorkStudyEvaluation;

public interface WorkStudyEvaluationService extends IService<WorkStudyEvaluation> {
    void submitEvaluation(WorkStudyEvaluation evaluation);
}