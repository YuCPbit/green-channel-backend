package edu.workstudy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.workstudy.entity.WorkStudyHire;

public interface WorkStudyHireService extends IService<WorkStudyHire> {

    /**
     * 审批录用
     */
    Long approveHire(Long applyId, Long approverId);
}