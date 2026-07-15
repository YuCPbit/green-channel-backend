package edu.greenchannel.integration;

import edu.greenchannel.common.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;

@Component
public class ExternalApiInterceptor implements HandlerInterceptor {
    private static final String START_ATTRIBUTE = "edu.greenchannel.integration.StartNanos";
    private final IntegrationRepository repository;
    private final String configuredApiKey;

    public ExternalApiInterceptor(
            IntegrationRepository repository,
            @Value("${app.integration.api-key:}") String configuredApiKey) {
        this.repository = repository;
        this.configuredApiKey = configuredApiKey;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(START_ATTRIBUTE, System.nanoTime());
        if (!StringUtils.hasText(configuredApiKey)) {
            save(request, false, "IntegrationKeyNotConfigured");
            throw new BusinessException(50300, "外部接口尚未配置");
        }
        String supplied = request.getHeader("X-API-Key");
        if (!StringUtils.hasText(supplied) || !constantTimeEquals(configuredApiKey, supplied)) {
            save(request, false, "InvalidApiKey");
            throw new BusinessException(40100, "外部接口鉴权失败");
        }
        return true;
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request, HttpServletResponse response, Object handler, Exception exception) {
        save(request, exception == null && response.getStatus() < 400,
                exception == null ? null : exception.getClass().getSimpleName());
    }

    private boolean constantTimeEquals(String expected, String supplied) {
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8), supplied.getBytes(StandardCharsets.UTF_8));
    }

    private void save(HttpServletRequest request, boolean success, String failureType) {
        Object start = request.getAttribute(START_ATTRIBUTE);
        long duration = start instanceof Long value
                ? Math.max(0, (System.nanoTime() - value) / 1_000_000) : 0;
        String clientId = request.getHeader("X-Client-Id");
        if (!StringUtils.hasText(clientId) || !clientId.matches("[A-Za-z0-9_-]{1,50}")) {
            clientId = "UNKNOWN";
        }
        try {
            repository.save(new IntegrationCallLog(
                    0, clientId, request.getMethod(), request.getRequestURI(), success,
                    duration, failureType, LocalDateTime.now()));
        } catch (RuntimeException ignored) {
            // 监控落库失败不能覆盖原接口结果，且不输出请求头或请求体。
        }
    }
}
