package edu.workstudy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.workstudy.entity.WorkStudyBatch;

public interface WorkStudyBatchService extends IService<WorkStudyBatch> {
    Long createBatch(WorkStudyBatch batch, Long userId);
}