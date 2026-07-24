package edu.greenchannel.subsidy.dto.request;

/**
 * 台账查询请求参数。
 */
public record LedgerQueryRequest(
        Long batchId,
        Integer disburseStatus,
        String studentName,
        Long collegeId,
        int page,
        int size
) {
    public LedgerQueryRequest {
        if (page < 1) page = 1;
        if (size < 1) size = 20;
    }
}
