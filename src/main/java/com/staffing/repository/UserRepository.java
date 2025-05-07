package com.staffing.repository;

import com.staffing.model.User;
import com.staffing.model.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @Cacheable(value = "users", key = "'email:' + #email")
    Optional<User> findByEmail(String email);
    
    @Cacheable(value = "users", key = "'exists:' + #email")
    boolean existsByEmail(String email);
    
    @Cacheable(value = "users", key = "'count:' + #role")
    long countByRole(UserRole role);
    
    @Query("SELECT COUNT(u) FROM User u")
    @Cacheable(value = "users", key = "'total'")
    long countTotalUsers();
} 