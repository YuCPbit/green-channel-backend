package edu.greenchannel.workstudy.service;

import com.baomidou.mybatisplus.spring.service.IService;
import edu.greenchannel.workstudy.entity.WorkStudyPosition;
import edu.greenchannel.workstudy.enums.WorkStudyStatus;

import java.util.List;

public interface WorkStudyPositionService extends IService<WorkStudyPosition> {
    /**
     * 发布岗位（用工部门）
     */
    Long publishPosition(WorkStudyPosition position, Long userId);

    /**
     * 提交审核
     */
    void submitForApproval(Long positionId, Long userId);

    /**
     * 审核岗位（学校资助中心）
     */
    void approvePosition(Long positionId, boolean approved, String rejectReason, Long userId);

    /**
     * 获取岗位列表（无分页，自动过滤已删除）
     */
    List<WorkStudyPosition> listValidPositions(Long batchId, Integer status);

    /**
     * 下架岗位
     */
    void offlinePosition(Long positionId, Long userId);

    /**
     * 更新岗位信息
     */
    void updatePosition(WorkStudyPosition position, Long userId);
}