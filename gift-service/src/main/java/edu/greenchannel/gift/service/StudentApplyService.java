package edu.greenchannel.gift.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.greenchannel.gift.dto.review.StudentApplyUpdateDTO;
import edu.greenchannel.gift.entity.StudentApply;

public interface StudentApplyService extends IService<StudentApply> {

    /**
     * 驳回后修改申请，重新提交至辅导员审核
     */
    void reSubmitAfterReject(StudentApplyUpdateDTO dto);
}