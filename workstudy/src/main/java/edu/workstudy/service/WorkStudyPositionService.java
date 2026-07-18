package edu.workstudy.service;

import edu.workstudy.entity.WorkStudyPosition;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 岗位服务接口
 * 必须继承MyBatis-Plus的IService，否则ServiceImpl无法识别
 */
public interface WorkStudyPositionService extends IService<WorkStudyPosition> {

    /**
     * 新增岗位
     */
    Long addPosition(WorkStudyPosition position);

    /**
     * 发布岗位（核心方法）
     * @param batchId 批次ID
     * @param position 岗位信息
     * @param userId 发布人ID
     * @return 岗位ID
     */
    Long publish(Long batchId, WorkStudyPosition position, Long userId);
}