package com.staffing.security;

import com.staffing.model.enums.UserRole;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresRole {
    UserRole[] value();
    boolean requireWrite() default false;
    boolean requireUserManagement() default false;
    boolean requireAssignmentManagement() default false;
} 