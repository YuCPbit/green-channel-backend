package edu.greenchannel.integration;

import edu.greenchannel.common.BusinessException;
import edu.greenchannel.common.PageResult;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExternalApiInterceptorTest {
    @Test
    void acceptsConfiguredKeyAndRecordsOnlySafeMetadata() throws Exception {
        RecordingRepository repository = new RecordingRepository();
        ExternalApiInterceptor interceptor = new ExternalApiInterceptor(repository, "test-only-key");
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/external/sso/verify");
        request.addHeader("X-API-Key", "test-only-key");
        request.addHeader("X-Client-Id", "MOCK_SSO");

        assertTrue(interceptor.preHandle(request, new MockHttpServletResponse(), new Object()));
        interceptor.afterCompletion(request, new MockHttpServletResponse(), new Object(), null);

        assertEquals(1, repository.logs.size());
        assertEquals("MOCK_SSO", repository.logs.get(0).clientId());
        assertEquals("/api/external/sso/verify", repository.logs.get(0).requestPath());
    }

    @Test
    void rejectsWrongKeyWithoutPersistingKeyValue() {
        RecordingRepository repository = new RecordingRepository();
        ExternalApiInterceptor interceptor = new ExternalApiInterceptor(repository, "test-only-key");
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/external/academic/students/1");
        request.addHeader("X-API-Key", "wrong-value");

        BusinessException error = assertThrows(BusinessException.class,
                () -> interceptor.preHandle(request, new MockHttpServletResponse(), new Object()));

        assertEquals(40100, error.getCode());
        assertEquals("InvalidApiKey", repository.logs.get(0).failureType());
    }

    private static class RecordingRepository implements IntegrationRepository {
        private final List<IntegrationCallLog> logs = new ArrayList<>();

        @Override
        public void save(IntegrationCallLog log) {
            logs.add(log);
        }

        @Override
        public PageResult<IntegrationCallLog> search(
                String clientId, Boolean success, int page, int size) {
            return new PageResult<>(List.copyOf(logs), logs.size(), page, size);
        }
    }
}
