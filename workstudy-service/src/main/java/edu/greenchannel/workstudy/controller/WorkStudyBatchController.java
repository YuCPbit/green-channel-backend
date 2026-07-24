package edu.greenchannel.workstudy.controller;

import edu.greenchannel.common.ApiResponse;
import edu.greenchannel.auth.AuthInterceptor;
import edu.greenchannel.auth.CurrentUser;
import edu.greenchannel.auth.RequirePermission;
import edu.greenchannel.workstudy.entity.WorkStudyBatch;
import edu.greenchannel.workstudy.service.WorkStudyBatchService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/batch")
public class WorkStudyBatchController {

    private final WorkStudyBatchService batchService;

    public WorkStudyBatchController(WorkStudyBatchService batchService) {
        this.batchService = batchService;
    }

    @PostMapping("/create")
    @RequirePermission("school:workstudy:edit")
    public ApiResponse<Long> createBatch(@RequestBody WorkStudyBatch batch,
            @RequestAttribute(AuthInterceptor.CURRENT_USER_ATTRIBUTE) CurrentUser currentUser) {
        Long batchId = batchService.createBatch(batch, currentUser.id());
        return ApiResponse.success(batchId);
    }

    @GetMapping("/list")
    @RequirePermission("school:workstudy:view")
    public ApiResponse<?> listBatches() {
        return ApiResponse.success(batchService.list());
    }
}
