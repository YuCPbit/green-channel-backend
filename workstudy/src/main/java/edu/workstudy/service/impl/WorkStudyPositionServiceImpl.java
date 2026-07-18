package edu.workstudy.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.workstudy.entity.WorkStudyBatch;
import edu.workstudy.entity.WorkStudyPosition;
import edu.workstudy.mapper.WorkStudyBatchMapper;
import edu.workstudy.mapper.WorkStudyPositionMapper;
import edu.workstudy.service.WorkStudyPositionService;
import org.springframework.stereotype.Service;

@Service
public class WorkStudyPositionServiceImpl
        extends ServiceImpl<WorkStudyPositionMapper, WorkStudyPosition>
        implements WorkStudyPositionService {

    private final WorkStudyBatchMapper batchMapper;

    public WorkStudyPositionServiceImpl(WorkStudyBatchMapper batchMapper) {
        this.batchMapper = batchMapper;
    }

    @Override
    public Long publishPosition(WorkStudyPosition position, Long userId) {
        // 校验批次是否存在且有效
        WorkStudyBatch batch = batchMapper.selectById(position.getBatchId());
        if (batch == null || batch.getIsDeleted() == 1) {
            throw new RuntimeException("所属批次不存在或已删除");
        }
        // 校验薪酬标准（读取系统参数 WS_MIN_HOURLY_RATE）
        // 这里先硬编码，后续对接A的系统参数模块
        if (position.getSalaryRate().compareTo(new java.math.BigDecimal("12.00")) < 0) {
            throw new RuntimeException("薪酬标准不得低于最低工资标准");
        }

        position.setPublisherId(userId);
        position.setStatus(1); // 待审核
        position.setHiredCount(0); // 初始录用数为0
        save(position);
        return position.getId();
    }
}