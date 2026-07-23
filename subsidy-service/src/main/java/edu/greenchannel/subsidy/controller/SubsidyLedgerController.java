package edu.greenchannel.subsidy.controller;

import com.alibaba.excel.EasyExcel;
import edu.greenchannel.auth.AuthInterceptor;
import edu.greenchannel.auth.CurrentUser;
import edu.greenchannel.auth.RequirePermission;
import edu.greenchannel.common.ApiResponse;
import edu.greenchannel.common.PageResult;
import edu.greenchannel.subsidy.dto.request.DisburseBatchRequest;
import edu.greenchannel.subsidy.dto.response.LedgerSummaryResponse;
import edu.greenchannel.subsidy.dto.response.LedgerView;
import edu.greenchannel.subsidy.dto.response.LedgerDisburseStatusResponse;
import edu.greenchannel.subsidy.service.SubsidyLedgerService;
import edu.greenchannel.subsidy.service.SubsidyApplyService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 补助发放台账控制器。
 * 提供台账查询、发放确认、汇总统计和 Excel 导出。
 */
@RestController
@RequestMapping("/api/subsidy/ledger")
public class SubsidyLedgerController {

    private final SubsidyLedgerService ledgerService;
    private final SubsidyApplyService applyService;

    public SubsidyLedgerController(SubsidyLedgerService ledgerService,
                                    SubsidyApplyService applyService) {
        this.ledgerService = ledgerService;
        this.applyService = applyService;
    }

    /** GET /api/subsidy/ledger — 分页查询台账列表 */
    @GetMapping
    @RequirePermission("school:fund:view")
    public ApiResponse<PageResult<LedgerView>> list(
            @RequestParam(required = false) Long batchId,
            @RequestParam(required = false) Integer disburseStatus,
            @RequestParam(required = false) String studentName,
            @RequestParam(required = false) Long collegeId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(ledgerService.search(
                batchId, disburseStatus, studentName, collegeId, page, size));
    }

    /** GET /api/subsidy/ledger/summary — 台账汇总统计 */
    @GetMapping("/summary")
    @RequirePermission("school:fund:view")
    public ApiResponse<LedgerSummaryResponse> summary(@RequestParam(required = false) Long batchId) {
        return ApiResponse.success(ledgerService.getSummary(batchId));
    }

    /** GET /api/subsidy/ledger/{id} — 台账详情 */
    @GetMapping("/{id}")
    @RequirePermission("school:fund:view")
    public ApiResponse<LedgerView> detail(@PathVariable long id) {
        return ApiResponse.success(ledgerService.getDetail(id));
    }

    /** GET /api/subsidy/ledger/by-apply/{applyId} — 按申请ID查询发放状态（学生端查看） */
    @GetMapping("/by-apply/{applyId}")
    @RequirePermission("student:subsidy:view")
    public ApiResponse<LedgerDisburseStatusResponse> getByApplyId(
            @PathVariable long applyId,
            @RequestAttribute(AuthInterceptor.CURRENT_USER_ATTRIBUTE) CurrentUser currentUser) {
        applyService.getApplyDetail(currentUser, applyId);
        return ApiResponse.success(ledgerService.getDisburseStatusByApplyId(applyId));
    }

    /** PUT /api/subsidy/ledger/{id}/disburse — 单笔确认发放 */
    @PutMapping("/{id}/disburse")
    @RequirePermission("school:fund:view")
    public ApiResponse<Void> confirmDisburse(
            @PathVariable long id,
            @RequestAttribute(AuthInterceptor.CURRENT_USER_ATTRIBUTE) CurrentUser currentUser) {
        ledgerService.confirmDisburse(id, currentUser);
        return ApiResponse.success();
    }

    /** PUT /api/subsidy/ledger/batch-disburse — 批量确认发放 */
    @PutMapping("/batch-disburse")
    @RequirePermission("school:fund:view")
    public ApiResponse<Void> batchDisburse(
            @RequestBody DisburseBatchRequest request,
            @RequestAttribute(AuthInterceptor.CURRENT_USER_ATTRIBUTE) CurrentUser currentUser) {
        ledgerService.batchConfirmDisburse(request.ledgerIds(), currentUser);
        return ApiResponse.success();
    }

    /** GET /api/subsidy/ledger/export — 导出 Excel */
    @GetMapping("/export")
    @RequirePermission("school:fund:view")
    public void exportExcel(
            @RequestParam(required = false) Long batchId,
            @RequestParam(required = false) Integer disburseStatus,
            @RequestParam(required = false) Long collegeId,
            HttpServletResponse response) throws IOException {

        List<LedgerView> data = ledgerService.getExportData(batchId, disburseStatus, collegeId);

        // 设置响应头
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("补助发放台账", StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");
        response.setHeader("Content-disposition",
                "attachment;filename*=utf-8''" + fileName + ".xlsx");

        // 中文表头
        List<List<String>> headers = List.of(
                List.of("序号"), List.of("批次名称"), List.of("申请编号"),
                List.of("学号"), List.of("姓名"), List.of("学院"),
                List.of("年级"), List.of("补助类型"), List.of("审批金额"),
                List.of("发放状态"), List.of("发放时间"), List.of("银行卡号"),
                List.of("备注"));

        // 数据行
        int[] index = {1};
        List<List<Object>> rows = data.stream()
                .map(item -> List.<Object>of(
                        index[0]++, item.batchName(), item.applyNo(),
                        item.studentNo(), item.studentName(), item.collegeName(),
                        item.grade() != null ? item.grade() : "",
                        item.subsidyTypeName(), item.approvedAmount(),
                        item.disburseStatusName(), item.disburseTime(),
                        item.bankCardNo() != null ? item.bankCardNo() : "",
                        item.remark() != null ? item.remark() : ""))
                .toList();

        EasyExcel.write(response.getOutputStream())
                .head(headers)
                .sheet("补助发放台账")
                .doWrite(rows);
    }
}
