package edu.greenchannel.workstudy.service;

import com.baomidou.mybatisplus.spring.service.IService;
import edu.greenchannel.workstudy.entity.WorkStudyHire;

public interface WorkStudyHireService extends IService<WorkStudyHire> {

    /**
     * 审批录用（资助中心）
     */
    Long approveHire(Long applyId, Long approverId);

    /**
     * 学生离岗
     */
    void leavePosition(Long hireId, Integer leaveType, String reason, Long operatorId);
}