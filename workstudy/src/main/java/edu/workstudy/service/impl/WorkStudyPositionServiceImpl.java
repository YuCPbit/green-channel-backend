package edu.workstudy.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.workstudy.entity.WorkStudyPosition;
import edu.workstudy.mapper.WorkStudyPositionMapper;
import edu.workstudy.service.WorkStudyPositionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 岗位服务实现类
 */
@Service
public class WorkStudyPositionServiceImpl
        extends ServiceImpl<WorkStudyPositionMapper, WorkStudyPosition>
        implements WorkStudyPositionService {

    private final WorkStudyPositionMapper positionMapper;

    public WorkStudyPositionServiceImpl(WorkStudyPositionMapper positionMapper) {
        this.positionMapper = positionMapper;
    }

    @Override
    public Long addPosition(WorkStudyPosition position) {
        positionMapper.insert(position);
        return position.getId();
    }

    /**
     * 发布岗位核心逻辑
     * 加事务注解，保证数据一致性（比如扣减名额失败时自动回滚）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long publish(Long batchId, WorkStudyPosition position, Long userId) {
        position.setBatchId(batchId);
        position.setPublisherId(userId);
        position.setStatus(2); // 2=已上架（和数据库字典值对应）
        position.setHiredCount(0); // 初始录用人数为0
        save(position); // 继承自ServiceImpl的方法，不用自己实现
        return position.getId();
    }
}