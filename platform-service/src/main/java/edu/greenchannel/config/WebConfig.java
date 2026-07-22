package edu.greenchannel.config;

import edu.greenchannel.integration.ExternalApiInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final ExternalApiInterceptor externalApiInterceptor;

    public WebConfig(ExternalApiInterceptor externalApiInterceptor) {
        this.externalApiInterceptor = externalApiInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(externalApiInterceptor)
                .addPathPatterns("/api/external/**")
                .order(1);
    }

}
