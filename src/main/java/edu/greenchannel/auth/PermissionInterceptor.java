package edu.greenchannel.auth;

import edu.greenchannel.common.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class PermissionInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }
        RequirePermission requirement = handlerMethod.getMethodAnnotation(RequirePermission.class);
        if (requirement == null) {
            requirement = handlerMethod.getBeanType().getAnnotation(RequirePermission.class);
        }
        if (requirement == null) {
            return true;
        }
        CurrentUser user = (CurrentUser) request.getAttribute(AuthInterceptor.CURRENT_USER_ATTRIBUTE);
        if (user == null || !user.permissions().contains(requirement.value())) {
            throw new BusinessException(40300, "无权执行此操作");
        }
        return true;
    }
}

