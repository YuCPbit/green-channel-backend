package edu.greenchannel.subsidy.controller;

import edu.greenchannel.common.ApiResponse;
import edu.greenchannel.subsidy.dto.request.AllocationCreateRequest;
import edu.greenchannel.subsidy.dto.response.AllocationItemResponse;
import edu.greenchannel.subsidy.dto.response.AllocationSummaryResponse;
import edu.greenchannel.subsidy.entity.College;
import edu.greenchannel.subsidy.service.SubsidyAllocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subsidy/allocations")
public class SubsidyAllocationController {

    @Autowired
    private SubsidyAllocationService allocationService;

    /** POST /api/subsidy/allocations — 下发额度（学校→学院 或 学院→年级） */
    @PostMapping
    public ApiResponse<String> allocateQuota(
            @RequestBody AllocationCreateRequest request,
            @RequestHeader(value = "X-User-Role", defaultValue = "1") Integer currentUserRole,
            @RequestHeader(value = "X-College-Id", defaultValue = "1") Long currentUserCollegeId
    ) {
        allocationService.allocateQuota(request, currentUserRole, currentUserCollegeId);
        return ApiResponse.success("额度分配下发成功！");
    }

    /** GET /api/subsidy/allocations/summary — 额度看板汇总 */
    @GetMapping("/summary")
    public ApiResponse<AllocationSummaryResponse> getSummary(
            @RequestParam Long batchId,
            @RequestHeader(value = "X-User-Role", defaultValue = "1") Integer currentUserRole,
            @RequestHeader(value = "X-College-Id", defaultValue = "1") Long currentUserCollegeId
    ) {
        AllocationSummaryResponse summary = allocationService.getSummary(batchId, currentUserRole, currentUserCollegeId);
        return ApiResponse.success(summary);
    }

    /** GET /api/subsidy/allocations — 查询分配明细列表 */
    @GetMapping
    public ApiResponse<List<AllocationItemResponse>> listAllocations(
            @RequestParam Long batchId,
            @RequestParam(required = false) Integer targetType
    ) {
        List<AllocationItemResponse> list = allocationService.listAllocations(batchId, targetType);
        return ApiResponse.success(list);
    }

    /** GET /api/subsidy/colleges — 获取学院列表 */
    @GetMapping("/colleges")
    public ApiResponse<List<College>> listColleges() {
        return ApiResponse.success(allocationService.listColleges());
    }

    /** GET /api/subsidy/grades — 获取年级列表 */
    @GetMapping("/grades")
    public ApiResponse<List<Integer>> listGrades() {
        return ApiResponse.success(allocationService.listGrades());
    }
}
