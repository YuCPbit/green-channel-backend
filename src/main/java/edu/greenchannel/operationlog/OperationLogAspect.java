package edu.greenchannel.operationlog;

import edu.greenchannel.auth.AuthInterceptor;
import edu.greenchannel.auth.CurrentUser;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

@Aspect
@Component
public class OperationLogAspect {
    private static final Logger log = LoggerFactory.getLogger(OperationLogAspect.class);
    private final OperationLogRepository repository;
    private final SpelExpressionParser expressionParser = new SpelExpressionParser();

    public OperationLogAspect(OperationLogRepository repository) {
        this.repository = repository;
    }

    @Around("@annotation(operationLog)")
    public Object record(ProceedingJoinPoint joinPoint, OperationLog operationLog) throws Throwable {
        HttpServletRequest request = currentRequest();
        CurrentUser user = request == null ? null :
                (CurrentUser) request.getAttribute(AuthInterceptor.CURRENT_USER_ATTRIBUTE);
        Object result = null;
        boolean success = false;
        String description = "success";
        try {
            result = joinPoint.proceed();
            success = true;
            return result;
        } catch (Throwable throwable) {
            description = safeMessage(throwable);
            throw throwable;
        } finally {
            String targetId = resolveTargetId(joinPoint, operationLog.targetId(), result);
            OperationLogEntry entry = new OperationLogEntry(
                    0, user == null ? null : user.id(), operationLog.module(), operationLog.action(), targetId,
                    request == null ? null : request.getMethod(), request == null ? null : request.getRequestURI(),
                    clientIp(request), success, description, LocalDateTime.now());
            try {
                repository.save(entry);
            } catch (RuntimeException exception) {
                log.warn("Operation log persistence failed for module={} action={}",
                        operationLog.module(), operationLog.action(), exception);
            }
        }
    }

    private String resolveTargetId(ProceedingJoinPoint joinPoint, String expressionText, Object result) {
        if (!StringUtils.hasText(expressionText)) {
            return null;
        }
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            StandardEvaluationContext context = new StandardEvaluationContext();
            Object[] arguments = joinPoint.getArgs();
            String[] parameterNames = signature.getParameterNames();
            if (parameterNames != null) {
                for (int index = 0; index < arguments.length; index++) {
                    context.setVariable(parameterNames[index], arguments[index]);
                }
            }
            context.setVariable("result", result);
            Expression expression = expressionParser.parseExpression(expressionText);
            Object value = expression.getValue(context);
            return value == null ? null : String.valueOf(value);
        } catch (RuntimeException exception) {
            log.debug("Unable to resolve operation log target expression {}", expressionText, exception);
            return null;
        }
    }

    private HttpServletRequest currentRequest() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            return attributes.getRequest();
        }
        return null;
    }

    private String clientIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String forwarded = request.getHeader("X-Forwarded-For");
        return StringUtils.hasText(forwarded) ? forwarded.split(",")[0].trim() : request.getRemoteAddr();
    }

    private String safeMessage(Throwable throwable) {
        return throwable.getClass().getSimpleName();
    }
}
