package edu.greenchannel.subsidy.dto.request;

/**
 * 确认发放请求（单笔）。
 */
public record DisburseRequest(
        long ledgerId,
        String remark
) {}
