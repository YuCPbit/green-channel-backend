package edu.greenchannel.workstudy.service;

import com.baomidou.mybatisplus.spring.service.IService;
import edu.greenchannel.workstudy.entity.WorkStudyAgreement;
import edu.greenchannel.workstudy.entity.WorkStudyHire;

public interface WorkStudyAgreementService extends IService<WorkStudyAgreement> {
    /**
     * 生成协议（在录用审批通过时调用）
     */
    void generateAgreement(WorkStudyHire hire);

    /**
     * 学生签署协议
     */
    void signAgreement(Long agreementId, Long studentId);
}
