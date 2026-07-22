package edu.greenchannel.subsidy.service;

import edu.greenchannel.subsidy.dto.request.*;
import edu.greenchannel.subsidy.dto.response.BatchResponse;
import org.springframework.data.domain.Page;

public interface SubsidyBatchService {
    BatchResponse createBatch(BatchCreateRequest request);
    Page<BatchResponse> queryBatches(BatchQueryRequest request);
    BatchResponse updateBatch(Long id, BatchUpdateRequest request);
    BatchResponse startBatch(Long id);
    BatchResponse endBatch(Long id);
}