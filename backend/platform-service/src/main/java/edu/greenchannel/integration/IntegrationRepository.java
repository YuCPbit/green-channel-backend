package edu.greenchannel.integration;

import edu.greenchannel.common.PageResult;

public interface IntegrationRepository {
    void save(IntegrationCallLog log);

    PageResult<IntegrationCallLog> search(
            String clientId, Boolean success, int page, int size);
}
