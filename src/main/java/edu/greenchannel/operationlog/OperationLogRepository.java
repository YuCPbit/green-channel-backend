package edu.greenchannel.operationlog;

import edu.greenchannel.common.PageResult;

import java.time.LocalDateTime;

public interface OperationLogRepository {
    void save(OperationLogEntry entry);

    PageResult<OperationLogEntry> search(
            String module, Long operatorId, String action, LocalDateTime startTime,
            LocalDateTime endTime, Boolean success, int page, int size);
}
