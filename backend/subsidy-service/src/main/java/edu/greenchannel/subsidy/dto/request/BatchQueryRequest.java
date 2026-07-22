package edu.greenchannel.subsidy.dto.request;
public record BatchQueryRequest(
        String batchName,
        Integer status,
        int page,
        int size
) {}