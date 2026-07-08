package com.aiart.platform.security;

import com.aiart.platform.exception.BusinessException;
import com.aiart.platform.exception.ErrorCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUser {
    public Long userIdOrNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        try {
            return Long.valueOf(String.valueOf(authentication.getPrincipal()));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public Long requireUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        try {
            return Long.valueOf(String.valueOf(authentication.getPrincipal()));
        } catch (NumberFormatException ex) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
    }

    public void requireAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        boolean admin = authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
        if (!admin) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Admin role required");
        }
    }
}
