package com.logmaster.api.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtil {

    public Long getCurrentCompanyId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (Long) auth.getCredentials();
    }

    public String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (String) auth.getPrincipal();
    }
}