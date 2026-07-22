package edu.greenchannel.auth;

import edu.greenchannel.common.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    public static final String CURRENT_USER_ATTRIBUTE = "edu.greenchannel.auth.CurrentUser";
    private final TokenService tokenService;

    public AuthInterceptor(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new BusinessException(40100, "请先登录");
        }
        CurrentUser user = tokenService.resolve(authorization.substring(7).trim())
                .orElseThrow(() -> new BusinessException(40100, "登录已失效，请重新登录"));
        request.setAttribute(CURRENT_USER_ATTRIBUTE, user);
        return true;
    }
}
