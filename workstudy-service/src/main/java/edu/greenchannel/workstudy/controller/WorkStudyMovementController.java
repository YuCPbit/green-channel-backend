package edu.greenchannel.workstudy.controller;

import edu.greenchannel.auth.AuthInterceptor;
import edu.greenchannel.auth.CurrentUser;
import edu.greenchannel.auth.RequirePermission;
import edu.greenchannel.common.ApiResponse;
import edu.greenchannel.workstudy.service.WorkStudyMovementService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/workstudy/movements")
@RequiredArgsConstructor
public class WorkStudyMovementController {

    private final WorkStudyMovementService movementService;

    @GetMapping("/positions")
    @RequirePermission("workstudy:movement:apply")
    public ApiResponse<List<Map<String, Object>>> availablePositions() {
        return ApiResponse.success(movementService.availablePositions());
    }

    @PostMapping
    @RequirePermission("workstudy:movement:apply")
    public ApiResponse<Map<String, Object>> create(
            @Valid @RequestBody MovementCreateRequest request,
            @RequestAttribute(AuthInterceptor.CURRENT_USER_ATTRIBUTE) CurrentUser user) {
        return ApiResponse.success(Map.of("id", movementService.create(request, user)));
    }

    @GetMapping("/my")
    @RequirePermission("workstudy:movement:apply")
    public ApiResponse<List<Map<String, Object>>> myMovements(
            @RequestAttribute(AuthInterceptor.CURRENT_USER_ATTRIBUTE) CurrentUser user) {
        return ApiResponse.success(movementService.myMovements(user.id()));
    }

    @GetMapping("/pending")
    @RequirePermission("workstudy:movement:review")
    public ApiResponse<List<Map<String, Object>>> pendingMovements() {
        return ApiResponse.success(movementService.pendingMovements());
    }

    @PostMapping("/{id}/review")
    @RequirePermission("workstudy:movement:review")
    public ApiResponse<Void> review(
            @PathVariable long id,
            @Valid @RequestBody MovementReviewRequest request,
            @RequestAttribute(AuthInterceptor.CURRENT_USER_ATTRIBUTE) CurrentUser user) {
        movementService.review(id, request, user.id());
        return ApiResponse.success();
    }

    public record MovementCreateRequest(
            @NotNull Long hireId,
            @NotBlank String movementType,
            Long targetPositionId,
            @NotBlank String reason) {
    }

    public record MovementReviewRequest(@NotBlank String action, @NotBlank String comment) {
    }
}
