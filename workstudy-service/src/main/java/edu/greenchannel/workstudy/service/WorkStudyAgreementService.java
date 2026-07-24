package edu.greenchannel.workstudy.service;

import com.baomidou.mybatisplus.spring.service.IService;
import edu.greenchannel.workstudy.entity.WorkStudyAgreement;
import edu.greenchannel.workstudy.entity.WorkStudyHire;

import java.time.LocalDate;
import java.util.List;

public interface WorkStudyAgreementService extends IService<WorkStudyAgreement> {
    /**
     * 生成协议（在录用审批通过时调用）
     */
    void generateAgreement(WorkStudyHire hire);

    /**
     * 学生签署协议
     */
    void signAgreement(Long agreementId, Long studentId);

    /**
     * 续签已签署协议；未指定日期时默认在原到期日基础上延长六个月。
     */
    void renewAgreement(Long agreementId, Long operatorId, LocalDate newEndDate);

    /**
     * 查询协议详情。学生只能查询本人协议，管理角色可按权限查询。
     */
    WorkStudyAgreement getAccessibleAgreement(Long agreementId, Long userId, Integer userType);

    /**
     * 管理端协议列表。
     */
    List<WorkStudyAgreement> listAgreements(Long studentId, Integer signStatus);
}
