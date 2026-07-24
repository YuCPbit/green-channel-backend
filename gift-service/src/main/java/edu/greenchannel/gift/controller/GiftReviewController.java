package edu.greenchannel.gift.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import edu.greenchannel.common.ApiResponse;
import edu.greenchannel.auth.RequirePermission;
import edu.greenchannel.auth.AuthInterceptor;
import edu.greenchannel.auth.CurrentUser;
import edu.greenchannel.gift.dto.review.BatchSubmitDTO;
import edu.greenchannel.gift.dto.review.GiftPickupDTO;
import edu.greenchannel.gift.dto.review.GiftReviewOperateDTO;
import edu.greenchannel.gift.dto.review.GiftSupplementDTO;
import edu.greenchannel.gift.entity.review.ReviewRecord;
import edu.greenchannel.gift.service.review.GiftReviewService;
import edu.greenchannel.gift.service.StudentApplyService;
import edu.greenchannel.gift.service.GiftIdentityService;
import edu.greenchannel.gift.vo.StudentApplyDetailVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;


@RestController
@RequestMapping("/api/gift/review")
@RequiredArgsConstructor
public class GiftReviewController {

    private final GiftReviewService giftReviewService;
    private final StudentApplyService studentApplyService;
    private final GiftIdentityService identityService;


    // 分页查询待审批申请单
    @GetMapping("/list")
    @RequirePermission("gift:review:view")
    public ApiResponse<Page<StudentApplyDetailVO>> listWaitReview(
            @RequestParam(required = false) Long batchId,
            @RequestParam(required = false) String studentName,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        Page<StudentApplyDetailVO> page = giftReviewService.listWaitReview(batchId, studentName, pageNum, pageSize);
        return ApiResponse.success(page);
    }


    // 单条审批操作：通过/驳回修改/不通过/修改单据
    @PostMapping("/operate")
    @RequirePermission("gift:review:view")
    public ApiResponse<Void> reviewOperate(@RequestBody @Valid GiftReviewOperateDTO dto) {
        giftReviewService.reviewOperate(dto);
        return ApiResponse.success(null);
    }


    // 辅导员/学院批量提交至下一审核节点
    @PostMapping("/batch-submit")
    @RequirePermission("gift:review:view")
    public ApiResponse<Void> batchSubmit(@RequestBody @Valid BatchSubmitDTO dto) {
        giftReviewService.batchSubmit(dto);
        return ApiResponse.success(null);
    }


     // 学校管理员取消已终审通过的申请
    @PostMapping("/cancel/{applyId}")
    @RequirePermission("gift:review:school")
    public ApiResponse<Void> cancelPassApply(@PathVariable Long applyId) {
        giftReviewService.cancelPassApply(applyId);
        return ApiResponse.success(null);
    }


     // 查询申请完整审批时间线记录
    @GetMapping("/record/{applyId}")
    @RequirePermission({"student:green:view", "gift:review:view"})
    public ApiResponse<List<ReviewRecord>> getReviewRecord(
            @PathVariable Long applyId,
            @RequestAttribute(AuthInterceptor.CURRENT_USER_ATTRIBUTE) CurrentUser currentUser) {
        if (currentUser.userType() == 1) {
            studentApplyService.getMine(
                    applyId, identityService.requireStudentId(currentUser.id()));
        }
        List<ReviewRecord> recordList = giftReviewService.getApplyReviewRecord(applyId);
        return ApiResponse.success(recordList);
    }

    // 礼包核销：正常领取
    @PostMapping("/pickup")
    @RequirePermission("gift:pickup:manage")
    public ApiResponse<Void> pickup(@RequestBody @Valid GiftPickupDTO dto) {
        giftReviewService.pickup(dto);
        return ApiResponse.success(null);
    }

    // 礼包核销：登记领取异常
    @PostMapping("/pickup/exception")
    @RequirePermission("gift:pickup:manage")
    public ApiResponse<Void> pickupException(@RequestBody @Valid GiftPickupDTO dto) {
        giftReviewService.pickupException(dto);
        return ApiResponse.success(null);
    }

    // 礼包核销：异常补发
    @PostMapping("/pickup/reissue")
    @RequirePermission("gift:pickup:manage")
    public ApiResponse<Void> pickupReissue(@RequestBody @Valid GiftPickupDTO dto) {
        giftReviewService.pickupReissue(dto);
        return ApiResponse.success(null);
    }

    // 历史数据补录：直接创建终审通过申请
    @PostMapping("/supplement")
    @RequirePermission("gift:supplement:manage")
    public ApiResponse<Void> supplement(@RequestBody @Valid GiftSupplementDTO dto) {
        giftReviewService.supplement(dto);
        return ApiResponse.success(null);
    }
}
