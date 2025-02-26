package com.twentyfive.apaapilayer.configurations;

import com.twentyfive.apaapilayer.interceptors.OrderEnabledInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {
    private final OrderEnabledInterceptor orderEnabledInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(orderEnabledInterceptor)
                .addPathPatterns("/orders/**")
                .addPathPatterns("/cart/**")
                .excludePathPatterns("/orders/by-customer/**")
                .excludePathPatterns("/orders/cancel/**");
    }
}
