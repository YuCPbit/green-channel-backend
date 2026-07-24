package edu.greenchannel.subsidy.service;

import edu.greenchannel.auth.CurrentUser;
import edu.greenchannel.common.PageResult;
import edu.greenchannel.subsidy.dto.response.LedgerView;
import edu.greenchannel.subsidy.dto.response.LedgerSummaryResponse;
import edu.greenchannel.subsidy.dto.response.LedgerDisburseStatusResponse;
import edu.greenchannel.subsidy.entity.SubsidyApplyRecord;

import java.util.List;

/**
 * 补助发放台账服务接口。
 */
public interface SubsidyLedgerService {

    /** 终审通过时自动生成台账（由审核流程触发） */
    void generateLedger(SubsidyApplyRecord approvedApply);

    /** 台账分页查询（多条件筛选） */
    PageResult<LedgerView> search(Long batchId, Integer disburseStatus, String studentName,
                                  Long collegeId, int page, int size);

    /** 台账详情 */
    LedgerView getDetail(long id);

    /** 台账汇总统计 */
    LedgerSummaryResponse getSummary(Long batchId);

    /** 单笔确认发放 */
    void confirmDisburse(long id, CurrentUser operator);

    /** 批量确认发放 */
    void batchConfirmDisburse(List<Long> ids, CurrentUser operator);

    /** 导出数据（不分页） */
    List<LedgerView> getExportData(Long batchId, Integer disburseStatus, Long collegeId);

    /** 按申请ID查询发放状态（学生端查看，返回null表示台账未生成） */
    LedgerDisburseStatusResponse getDisburseStatusByApplyId(long applyId);
}
