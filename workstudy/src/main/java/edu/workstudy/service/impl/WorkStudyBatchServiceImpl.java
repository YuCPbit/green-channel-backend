package edu.workstudy.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.workstudy.entity.WorkStudyBatch;
import edu.workstudy.mapper.WorkStudyBatchMapper;
import edu.workstudy.service.WorkStudyBatchService;
import org.springframework.stereotype.Service;

@Service
public class WorkStudyBatchServiceImpl
        extends ServiceImpl<WorkStudyBatchMapper, WorkStudyBatch>
        implements WorkStudyBatchService {

    @Override
    public Long createBatch(WorkStudyBatch batch, Long userId) {
        // 校验时间逻辑
        if (batch.getRegisterEndTime().isBefore(batch.getRegisterStartTime())) {
            throw new RuntimeException("报名结束时间不能早于开始时间");
        }
        batch.setCreatorId(userId);
        batch.setStatus(0); // 默认未开始
        save(batch);
        return batch.getId();
    }
}