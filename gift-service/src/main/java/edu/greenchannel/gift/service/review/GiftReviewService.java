package edu.greenchannel.gift.service.review;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import edu.greenchannel.gift.dto.review.BatchSubmitDTO;
import edu.greenchannel.gift.dto.review.GiftPickupDTO;
import edu.greenchannel.gift.dto.review.GiftReviewOperateDTO;
import edu.greenchannel.gift.dto.review.GiftSupplementDTO;
import edu.greenchannel.gift.entity.review.ReviewRecord;
import edu.greenchannel.gift.vo.StudentApplyDetailVO;
import java.util.List;


public interface GiftReviewService extends IService<ReviewRecord> {

    Page<StudentApplyDetailVO> listWaitReview(Long batchId, String studentName, Integer pageNum, Integer pageSize);
    //单条单据审批操作：通过/驳回/不通过/修改单据
    void reviewOperate(GiftReviewOperateDTO dto);

    //辅导员/学院批量提交至下一审核节点
    void batchSubmit(BatchSubmitDTO dto);

    //学校管理员取消已终审通过的申请
    void cancelPassApply(Long applyId);

    //查询该申请完整审批流水记录
    List<ReviewRecord> getApplyReviewRecord(Long applyId);

    // 礼包核销：正常领取
    void pickup(GiftPickupDTO dto);

    // 礼包核销：登记领取异常
    void pickupException(GiftPickupDTO dto);

    // 礼包核销：异常补发
    void pickupReissue(GiftPickupDTO dto);

    // 历史数据补录：直接创建终审通过的申请单
    void supplement(GiftSupplementDTO dto);
}