package edu.workstudy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.workstudy.entity.WorkStudyAgreement;

public interface WorkStudyAgreementService extends IService<WorkStudyAgreement> {
    /**
     * 生成协议（在录用审批通过时调用）
     */
    void generateAgreement(Long hireId);

    /**
     * 学生签署协议
     */
    void signAgreement(Long agreementId, Long studentId);
}