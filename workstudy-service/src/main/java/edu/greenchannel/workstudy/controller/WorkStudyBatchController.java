package edu.greenchannel.workstudy.controller;

import edu.greenchannel.auth.AuthInterceptor;
import edu.greenchannel.auth.CurrentUser;
import edu.greenchannel.auth.RequirePermission;
import edu.greenchannel.common.ApiResponse;
import edu.greenchannel.common.BusinessException;
import edu.greenchannel.workstudy.entity.WorkStudyBatch;
import edu.greenchannel.workstudy.enums.WorkStudyStatus;
import edu.greenchannel.workstudy.service.WorkStudyBatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workstudy/batch")
@RequiredArgsConstructor
public class WorkStudyBatchController {

    private final WorkStudyBatchService batchService;

    /**
     * 创建批次
     */
    @PostMapping
    @RequirePermission("workstudy:batch:create")
    public ApiResponse<Long> createBatch(@RequestBody WorkStudyBatch batch,
                                         @RequestAttribute(AuthInterceptor.CURRENT_USER_ATTRIBUTE) CurrentUser currentUser) {
        Long batchId = batchService.createBatch(batch, currentUser.id());
        return ApiResponse.success(batchId);
    }

    /**
     * 获取批次列表
     */
    @GetMapping("/list")
    @RequirePermission("workstudy:batch:view")
    public ApiResponse<List<WorkStudyBatch>> listBatches() {
        List<WorkStudyBatch> batches = batchService.listValidBatches();
        return ApiResponse.success(batches);
    }

    /**
     * 获取当前有效批次
     */
    @GetMapping("/current")
    @RequirePermission("workstudy:batch:view")
    public ApiResponse<WorkStudyBatch> getCurrentBatch() {
        WorkStudyBatch batch = batchService.getCurrentBatch();
        if (batch == null) {
            throw new BusinessException(40400, "当前无进行中的批次");
        }
        return ApiResponse.success(batch);
    }

    /**
     * 更新批次状态
     */
    @PutMapping("/{batchId}/status")
    @RequirePermission("workstudy:batch:update")
    public ApiResponse<Void> updateBatchStatus(@PathVariable Long batchId,
                                               @RequestParam Integer status,
                                               @RequestAttribute(AuthInterceptor.CURRENT_USER_ATTRIBUTE) CurrentUser currentUser) {
        WorkStudyStatus newStatus = WorkStudyStatus.fromCode(status);
        batchService.updateBatchStatus(batchId, newStatus);
        return ApiResponse.success();
    }

    /**
     * 删除批次
     */
    @DeleteMapping("/{batchId}")
    @RequirePermission("workstudy:batch:delete")
    public ApiResponse<Void> deleteBatch(@PathVariable Long batchId,
                                         @RequestAttribute(AuthInterceptor.CURRENT_USER_ATTRIBUTE) CurrentUser currentUser) {
        batchService.deleteBatch(batchId);
        return ApiResponse.success();
    }
}