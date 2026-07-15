package edu.greenchannel.operationlog;

import java.time.LocalDateTime;

public record OperationLogEntry(
        long id,
        Long operatorId,
        String module,
        String action,
        String targetId,
        String requestMethod,
        String requestUrl,
        String ipAddress,
        boolean success,
        String description,
        LocalDateTime operationTime
) {
}
