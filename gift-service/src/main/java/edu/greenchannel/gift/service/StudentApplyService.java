package edu.greenchannel.gift.service;

import com.baomidou.mybatisplus.spring.service.IService;
import edu.greenchannel.gift.dto.review.StudentApplyUpdateDTO;
import edu.greenchannel.gift.entity.StudentApply;

import java.util.List;

public interface StudentApplyService extends IService<StudentApply> {

    /**
     * 驳回后修改申请，重新提交至辅导员审核
     */
    void reSubmitAfterReject(StudentApplyUpdateDTO dto, Long studentId);

    Long submit(StudentApply apply, Long studentId);

    StudentApply getMine(Long applyId, Long studentId);

    List<StudentApply> listMine(Long studentId, Long packBatchId);
}
