package com.staffing.service;

import com.staffing.dto.UserDTO;
import com.staffing.model.User;
import com.staffing.model.enums.UserRole;
import com.staffing.repository.UserRepository;
import com.staffing.security.SecurityUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityUtils securityUtils;

    @Cacheable(value = "users", key = "'all'")
    public List<UserDTO> getAllUsers() {
        if (!securityUtils.isAdmin()) {
            throw new AccessDeniedException("Seuls les administrateurs peuvent voir la liste des utilisateurs");
        }
        return userRepository.findAll().stream()
                .map(UserDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "users", key = "#id")
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé avec l'id: " + id));
        
        if (!securityUtils.isAdmin() && !securityUtils.getCurrentUserId().equals(id)) {
            throw new AccessDeniedException("Vous n'avez pas le droit de voir cet utilisateur");
        }
        
        return UserDTO.fromEntity(user);
    }

    @Cacheable(value = "users", key = "#email")
    public UserDTO getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé avec l'email: " + email));
        return UserDTO.fromEntity(user);
    }

    @CachePut(value = "users", key = "#result.id")
    public UserDTO createUser(UserDTO userDTO) {
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new IllegalArgumentException("Un utilisateur existe déjà avec cet email");
        }

        if (userDTO.getPassword() == null || userDTO.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Le mot de passe est requis pour la création d'un utilisateur");
        }

        User user = userDTO.toEntity();
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        
        return UserDTO.fromEntity(userRepository.save(user));
    }

    @CachePut(value = "users", key = "#id")
    public UserDTO updateUser(Long id, UserDTO userDTO) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé avec l'id: " + id));

        if (!existingUser.getEmail().equals(userDTO.getEmail()) && 
            userRepository.existsByEmail(userDTO.getEmail())) {
            throw new IllegalArgumentException("Un utilisateur existe déjà avec cet email");
        }

        existingUser.setFirstName(userDTO.getFirstName());
        existingUser.setLastName(userDTO.getLastName());
        existingUser.setEmail(userDTO.getEmail());
        existingUser.setPhone(userDTO.getPhone());
        existingUser.setPosition(userDTO.getPosition());
        existingUser.setRole(userDTO.getRole());

        if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }

        return UserDTO.fromEntity(userRepository.save(existingUser));
    }

    @CacheEvict(value = "users", key = "#id")
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé avec l'id: " + id));

        // Vérifier qu'on ne supprime pas l'utilisateur connecté
        User currentUser = userRepository.findByEmail(securityUtils.getCurrentUser().getEmail())
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur connecté non trouvé"));
        
        if (user.getId().equals(currentUser.getId())) {
            throw new IllegalStateException("Vous ne pouvez pas supprimer votre propre compte");
        }

        // Vérifier qu'on ne supprime pas le dernier admin
        if (user.getRole() == UserRole.ADMIN && userRepository.countByRole(UserRole.ADMIN) <= 1) {
            throw new IllegalStateException("Impossible de supprimer le dernier administrateur");
        }

        userRepository.delete(user);
    }

    @CachePut(value = "users", key = "#id")
    public void updateUserRole(Long id, String role) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé avec l'id: " + id));

        UserRole newRole = UserRole.valueOf(role.toUpperCase());
        
        // Vérifier qu'on ne modifie pas le rôle du dernier admin
        if (user.getRole() == UserRole.ADMIN && newRole != UserRole.ADMIN && 
            userRepository.countByRole(UserRole.ADMIN) <= 1) {
            throw new IllegalStateException("Impossible de modifier le rôle du dernier administrateur");
        }

        user.setRole(newRole);
        userRepository.save(user);
    }

    @Cacheable(value = "users", key = "#root.method.name")
    @Transactional(readOnly = true)
    public UserDTO getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("Aucun utilisateur connecté");
        }
        
        return userRepository.findByEmail(authentication.getName())
            .map(UserDTO::fromEntity)
            .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé"));
    }
} 