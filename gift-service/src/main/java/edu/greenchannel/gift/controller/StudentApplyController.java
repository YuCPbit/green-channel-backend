package edu.greenchannel.gift.controller;

import edu.greenchannel.auth.AuthInterceptor;
import edu.greenchannel.auth.CurrentUser;
import edu.greenchannel.auth.RequirePermission;
import edu.greenchannel.common.ApiResponse;
import edu.greenchannel.gift.dto.review.StudentApplyUpdateDTO;
import edu.greenchannel.gift.entity.StudentApply;
import edu.greenchannel.gift.service.StudentApplyService;
import edu.greenchannel.gift.service.GiftIdentityService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/gift/apply")
@RequirePermission("student:green:view")
public class StudentApplyController {

    private final StudentApplyService applyService;
    private final GiftIdentityService identityService;

    public StudentApplyController(StudentApplyService applyService, GiftIdentityService identityService) {
        this.applyService = applyService;
        this.identityService = identityService;
    }

    // 提交申请
    @PostMapping("/add")
    public ApiResponse<Long> add(
            @RequestBody StudentApply apply,
            @RequestAttribute(AuthInterceptor.CURRENT_USER_ATTRIBUTE) CurrentUser currentUser) {
        return ApiResponse.success(applyService.submit(
                apply, identityService.requireStudentId(currentUser.id())));
    }

    // 根据ID查询单条申请
    @GetMapping("/{id}")
    public ApiResponse<StudentApply> getById(
            @PathVariable Long id,
            @RequestAttribute(AuthInterceptor.CURRENT_USER_ATTRIBUTE) CurrentUser currentUser) {
        return ApiResponse.success(applyService.getMine(
                id, identityService.requireStudentId(currentUser.id())));
    }

    // 新增
    @GetMapping("/list")
    public ApiResponse<List<StudentApply>> list(
            @RequestParam(required = false) Long packBatchId,
            @RequestAttribute(AuthInterceptor.CURRENT_USER_ATTRIBUTE) CurrentUser currentUser
    ) {
        return ApiResponse.success(applyService.listMine(
                identityService.requireStudentId(currentUser.id()), packBatchId));
    }

    /**
     * 驳回后修改申请并重新提交至辅导员审核
     */
    @PostMapping("/resubmit")
    public ApiResponse<String> reSubmit(
            @RequestBody @Valid StudentApplyUpdateDTO dto,
            @RequestAttribute(AuthInterceptor.CURRENT_USER_ATTRIBUTE) CurrentUser currentUser) {
        applyService.reSubmitAfterReject(
                dto, identityService.requireStudentId(currentUser.id()));
        return ApiResponse.success("重新提交成功，待辅导员审核");
    }
}
