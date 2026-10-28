package com.supportdesk.security;

import com.supportdesk.entity.UserRole;
import com.supportdesk.exception.UnauthorizedAccessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {
        // Utility class
    }

    public static CustomUserDetails getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getPrincipal().equals("anonymousUser")) {
            throw new UnauthorizedAccessException("No authenticated user found");
        }
        return (CustomUserDetails) authentication.getPrincipal();
    }

    public static Long getCurrentUserId() {
        return getCurrentUser().getUserId();
    }

    public static Long getCurrentOrgId() {
        return getCurrentUser().getOrgId();
    }

    public static boolean isAdmin() {
        return getCurrentUser().getRole() == UserRole.ADMIN;
    }

    public static boolean isAgent() {
        return getCurrentUser().getRole() == UserRole.AGENT;
    }

    public static boolean isCustomer() {
        return getCurrentUser().getRole() == UserRole.CUSTOMER;
    }

    public static void validateOrgAccess(Long orgId) {
        Long currentOrgId = getCurrentOrgId();
        if (currentOrgId != null && !currentOrgId.equals(orgId)) {
            throw new UnauthorizedAccessException("Access denied: organization mismatch");
        }
    }
}
