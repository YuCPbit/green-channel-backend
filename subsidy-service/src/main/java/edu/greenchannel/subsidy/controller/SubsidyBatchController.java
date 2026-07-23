package edu.greenchannel.subsidy.controller;

import edu.greenchannel.common.ApiResponse;
import edu.greenchannel.auth.RequirePermission;
import edu.greenchannel.subsidy.dto.request.*;
import edu.greenchannel.subsidy.dto.response.BatchResponse;
import edu.greenchannel.subsidy.service.SubsidyBatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/subsidy/batches")
public class SubsidyBatchController {

    @Autowired
    private SubsidyBatchService batchService;

    // 1. POST /api/subsidy/batches：创建补助批次
    @PostMapping
    @RequirePermission("school:batch:view")
    public ApiResponse<BatchResponse> createBatch(@RequestBody BatchCreateRequest request) {
        BatchResponse response = batchService.createBatch(request);
        return ApiResponse.success(response);
    }

    // 2. GET /api/subsidy/batches：分页查询批次列表
    @GetMapping
    @RequirePermission({"school:batch:view", "college:quota:view"})
    public ApiResponse<Page<BatchResponse>> queryBatches(
            @RequestParam(required = false) String batchName,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        BatchQueryRequest query = new BatchQueryRequest(batchName, status, page, size);
        Page<BatchResponse> response = batchService.queryBatches(query);
        return ApiResponse.success(response);
    }

    // 3. PUT /api/subsidy/batches/{id}：修改批次
    @PutMapping("/{id}")
    @RequirePermission("school:batch:view")
    public ApiResponse<BatchResponse> updateBatch(
            @PathVariable Long id,
            @RequestBody BatchUpdateRequest request) {
        BatchResponse response = batchService.updateBatch(id, request);
        return ApiResponse.success(response);
    }

    // 4. POST /api/subsidy/batches/{id}/start：开始批次（DRAFT → ACTIVE）
    @PostMapping("/{id}/start")
    @RequirePermission("school:batch:view")
    public ApiResponse<BatchResponse> startBatch(@PathVariable Long id) {
        BatchResponse response = batchService.startBatch(id);
        return ApiResponse.success(response);
    }

    // 5. POST /api/subsidy/batches/{id}/end：提前结束批次（ACTIVE → ENDED）
    @PostMapping("/{id}/end")
    @RequirePermission("school:batch:view")
    public ApiResponse<BatchResponse> endBatch(@PathVariable Long id) {
        BatchResponse response = batchService.endBatch(id);
        return ApiResponse.success(response);
    }
}
