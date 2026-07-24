package edu.greenchannel.workstudy.controller;

import edu.greenchannel.auth.AuthInterceptor;
import edu.greenchannel.auth.CurrentUser;
import edu.greenchannel.auth.RequirePermission;
import edu.greenchannel.common.ApiResponse;
import edu.greenchannel.workstudy.service.WorkStudyHireService;
import edu.greenchannel.workstudy.service.WorkStudyIdentityService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/workstudy/hire")
@RequiredArgsConstructor
public class WorkStudyHireController {

    private final WorkStudyHireService hireService;
    private final WorkStudyIdentityService identityService;

    /**
     * 审批录用（资助中心）
     */
    @PostMapping("/approve")
    @RequirePermission("workstudy:hire:approve")
    public ApiResponse<Long> approveHire(@RequestParam Long applyId,
                                         @RequestAttribute(AuthInterceptor.CURRENT_USER_ATTRIBUTE) CurrentUser currentUser) {
        Long hireId = hireService.approveHire(applyId, currentUser.id());
        return ApiResponse.success(hireId);
    }

    /**
     * 学生离岗/教师解聘
     */
    @PostMapping("/{hireId}/leave")
    @RequirePermission("workstudy:hire:leave")
    public ApiResponse<Void> leavePosition(@PathVariable Long hireId,
                                           @RequestParam Integer leaveType,
                                           @RequestParam String reason,
                                           @RequestAttribute(AuthInterceptor.CURRENT_USER_ATTRIBUTE) CurrentUser currentUser) {
        hireService.leavePosition(hireId, leaveType, reason, currentUser.id());
        return ApiResponse.success();
    }

    @GetMapping("/my")
    @RequirePermission("workstudy:hire:view")
    public ApiResponse<?> myHires(
            @RequestParam(required = false) Integer hireStatus,
            @RequestAttribute(AuthInterceptor.CURRENT_USER_ATTRIBUTE) CurrentUser currentUser) {
        return ApiResponse.success(hireService.listHires(
                identityService.requireStudentId(currentUser.id()), hireStatus));
    }

    @GetMapping("/list")
    @RequirePermission({"workstudy:hire:approve", "workstudy:hire:leave"})
    public ApiResponse<?> listHires(
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Integer hireStatus) {
        return ApiResponse.success(hireService.listHires(studentId, hireStatus));
    }
}
