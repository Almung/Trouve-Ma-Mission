package com.staffing.security;

import com.staffing.model.User;
import com.staffing.model.enums.UserRole;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class RoleSecurityAspect {

    @Before("@annotation(com.staffing.security.RequiresRole)")
    public void checkRole(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        RequiresRole requiresRole = signature.getMethod().getAnnotation(RequiresRole.class);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("User not authenticated");
        }

        User user = (User) authentication.getPrincipal();
        UserRole userRole = user.getRole();
        boolean hasRequiredRole = false;

        for (UserRole role : requiresRole.value()) {
            if (userRole == role) {
                hasRequiredRole = true;
                break;
            }
        }

        if (!hasRequiredRole) {
            throw new SecurityException("Insufficient role privileges");
        }

        // Vérifier les permissions spécifiques
        if (requiresRole.requireWrite() && !userRole.canWrite()) {
            throw new SecurityException("Write permission required");
        }

        if (requiresRole.requireUserManagement() && !userRole.canManageUsers()) {
            throw new SecurityException("User management permission required");
        }

        if (requiresRole.requireAssignmentManagement() && !userRole.canManageAssignments()) {
            throw new SecurityException("Assignment management permission required");
        }
    }
} 