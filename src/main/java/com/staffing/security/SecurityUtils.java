package com.staffing.security;

import com.staffing.model.User;
import com.staffing.model.enums.UserRole;
import com.staffing.service.CustomUserDetailsService.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.cache.annotation.Cacheable;

@Component
public class SecurityUtils {

    @Cacheable(value = "currentUser", key = "'current'")
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails) {
            return ((CustomUserDetails) principal).getUser();
        }
        return null;
    }

    public Long getCurrentUserId() {
        User currentUser = getCurrentUser();
        return currentUser != null ? currentUser.getId() : null;
    }

    @Cacheable(value = "userRoles", key = "'isAdmin'")
    public boolean isAdmin() {
        User currentUser = getCurrentUser();
        return currentUser != null && currentUser.getRole() == UserRole.ADMIN;
    }

    @Cacheable(value = "userRoles", key = "'isManager'")
    public boolean isManager() {
        User currentUser = getCurrentUser();
        return currentUser != null && currentUser.getRole() == UserRole.MANAGER;
    }

    @Cacheable(value = "userRoles", key = "'canRead'")
    public boolean canRead() {
        return getCurrentUser() != null;
    }

    @Cacheable(value = "userRoles", key = "'canWrite'")
    public boolean canWrite() {
        User currentUser = getCurrentUser();
        return currentUser != null && 
               (currentUser.getRole() == UserRole.ADMIN || 
                currentUser.getRole() == UserRole.MANAGER);
    }

    @Cacheable(value = "userRoles", key = "'canManageUsers'")
    public boolean canManageUsers() {
        return isAdmin();
    }

    public boolean canManageAssignments() {
        User currentUser = getCurrentUser();
        return currentUser != null && 
               (currentUser.getRole() == UserRole.ADMIN || 
                currentUser.getRole() == UserRole.MANAGER);
    }

    public boolean isCurrentUser(Long userId) {
        Long currentUserId = getCurrentUserId();
        return currentUserId != null && currentUserId.equals(userId);
    }
} 