package com.staffing.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import com.staffing.model.User;
import com.staffing.model.enums.UserRole;
import com.staffing.repository.UserRepository;

@Component
public class InitialDataConfig implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Créer un utilisateur admin par défaut si aucun utilisateur n'existe
        userRepository.findByEmail("alainadmin@example.com").orElseGet(() -> {
            User adminUser = new User();
            adminUser.setFirstName("Alain");
            adminUser.setLastName("Tambwe");
            adminUser.setEmail("alainadmin@example.com");
            adminUser.setPassword(passwordEncoder.encode("admin@#123"));
            adminUser.setRole(UserRole.ADMIN);
            adminUser.setPosition("Administrator");
            adminUser.setPhone("+33123456789");
            return userRepository.save(adminUser);
        });
    }
} 