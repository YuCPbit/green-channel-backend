package edu.greenchannel.subsidy.repository;

import edu.greenchannel.common.PageResult;
import edu.greenchannel.subsidy.dto.response.LedgerView;
import edu.greenchannel.subsidy.dto.response.LedgerSummaryResponse;
import edu.greenchannel.subsidy.entity.SubsidyLedgerRecord;

import java.util.List;
import java.util.Optional;

/**
 * 补助发放台账数据访问接口。
 */
public interface SubsidyLedgerRepository {

    /** 插入台账记录，返回带ID的完整记录 */
    SubsidyLedgerRecord insert(SubsidyLedgerRecord record);

    /** 按ID查询 */
    Optional<SubsidyLedgerRecord> findById(long id);

    /** 按申请ID查询 */
    Optional<SubsidyLedgerRecord> findByApplyId(long applyId);

    /** 检查某申请是否已有台账记录 */
    boolean existsByApplyId(long applyId);

    /** 分页查询台账视图（多条件筛选，联表查询） */
    PageResult<LedgerView> search(Long batchId, Integer disburseStatus, String studentName,
                                  Long collegeId, int page, int size);

    /** 台账汇总统计 */
    LedgerSummaryResponse summary(Long batchId);

    /** 导出查询（不分页，返回全部匹配数据） */
    List<LedgerView> findAllForExport(Long batchId, Integer disburseStatus, Long collegeId);

    /** 更新单笔发放状态 */
    int updateDisburseStatus(long id, int status, Long operatorId);

    /** 更新发放信息（状态+备注） */
    int updateDisburseWithRemark(long id, int status, Long operatorId, String remark);

    /** 批量更新发放状态 */
    int batchUpdateDisburseStatus(List<Long> ids, int status, Long operatorId);
}
