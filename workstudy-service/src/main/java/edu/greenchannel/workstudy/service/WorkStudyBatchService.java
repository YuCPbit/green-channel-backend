package edu.greenchannel.workstudy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.greenchannel.workstudy.entity.WorkStudyBatch;

public interface WorkStudyBatchService extends IService<WorkStudyBatch> {
    Long createBatch(WorkStudyBatch batch, Long userId);
}