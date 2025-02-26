package com.twentyfive.apaapilayer.interceptors;

import com.twentyfive.apaapilayer.exceptions.SiteIsClosedException;
import com.twentyfive.apaapilayer.services.SettingService;
import com.twentyfive.apaapilayer.utils.JwtUtilities;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderEnabledInterceptor implements HandlerInterceptor {
    private final SettingService settingService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        List<String> roles = JwtUtilities.getRoles();
        if (roles.contains("customer") && !settingService.get().isOrdersEnabled()) {
            throw new SiteIsClosedException("Antica Pasticceria Rende is under maintenance! Thank you for the patience!");
        }
        return true;
    }
}
