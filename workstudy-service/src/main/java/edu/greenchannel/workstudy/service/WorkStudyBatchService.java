package edu.greenchannel.workstudy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.greenchannel.workstudy.entity.WorkStudyBatch;
import edu.greenchannel.workstudy.enums.WorkStudyStatus;
import java.util.List;

public interface WorkStudyBatchService extends IService<WorkStudyBatch> {

    /**
     * 创建批次
     */
    Long createBatch(WorkStudyBatch batch, Long userId);

    /**
     * 查询所有有效批次（未删除）
     */
    List<WorkStudyBatch> listValidBatches();

    /**
     * 获取当前正在进行中的批次
     */
    WorkStudyBatch getCurrentBatch();

    /**
     * 更新批次状态（带状态机校验）
     */
    void updateBatchStatus(Long batchId, WorkStudyStatus newStatus);

    /**
     * 逻辑删除批次
     */
    void deleteBatch(Long batchId);
}