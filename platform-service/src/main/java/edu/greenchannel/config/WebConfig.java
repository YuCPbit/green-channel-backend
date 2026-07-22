package edu.greenchannel.config;

import edu.greenchannel.auth.AuthInterceptor;
import edu.greenchannel.auth.PermissionInterceptor;
import edu.greenchannel.integration.ExternalApiInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final AuthInterceptor authInterceptor;
    private final PermissionInterceptor permissionInterceptor;
    private final ExternalApiInterceptor externalApiInterceptor;

    public WebConfig(AuthInterceptor authInterceptor, PermissionInterceptor permissionInterceptor,
                     ExternalApiInterceptor externalApiInterceptor) {
        this.authInterceptor = authInterceptor;
        this.permissionInterceptor = permissionInterceptor;
        this.externalApiInterceptor = externalApiInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(externalApiInterceptor)
                .addPathPatterns("/api/external/**")
                .order(1);
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/health", "/api/auth/login", "/api/external/**")
                .order(2);
        registry.addInterceptor(permissionInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/external/**")
                .order(3);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:5173")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}
