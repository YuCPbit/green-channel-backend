package edu.greenchannel.subsidy.dto.request;

import java.util.List;

/**
 * 批量确认发放请求。
 */
public record DisburseBatchRequest(
        List<Long> ledgerIds,
        String remark
) {}
