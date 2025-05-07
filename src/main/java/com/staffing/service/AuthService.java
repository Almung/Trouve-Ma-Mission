package com.staffing.service;

import com.staffing.dto.AuthResponseDTO;
import com.staffing.dto.LoginDTO;
import com.staffing.dto.UserDTO;
import com.staffing.model.User;
import com.staffing.model.enums.UserRole;
import com.staffing.repository.UserRepository;
import com.staffing.security.JwtTokenProvider;
import com.staffing.security.SecurityUtils;
import com.staffing.exception.AuthenticationException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final SecurityUtils securityUtils;
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    public AuthResponseDTO login(LoginDTO loginDTO) {
        try {
            logger.info("Tentative de connexion pour l'email: {}", loginDTO.getEmail());
            
            // Vérifier d'abord si l'utilisateur existe
            User user = userRepository.findByEmail(loginDTO.getEmail())
                .orElseThrow(() -> {
                    logger.error("Utilisateur non trouvé pour l'email: {}", loginDTO.getEmail());
                    return new AuthenticationException("Email ou mot de passe incorrect");
                });

            // Puis tenter l'authentification
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginDTO.getEmail(),
                    loginDTO.getPassword()
                )
            );
            
            logger.info("Authentification réussie pour l'email: {}", loginDTO.getEmail());
            logger.debug("Détails de l'authentification: {}", authentication);

            SecurityContextHolder.getContext().setAuthentication(authentication);

            logger.info("Utilisateur trouvé: {} (ID: {})", user.getEmail(), user.getId());
            logger.debug("Rôles de l'utilisateur: {}", user.getRole());
            
            String token = jwtTokenProvider.generateToken(authentication);
            logger.info("Token JWT généré avec succès");
            logger.debug("Token: {}", token);
            
            return new AuthResponseDTO(token, user);
        } catch (org.springframework.security.core.AuthenticationException e) {
            logger.error("Échec de l'authentification pour l'email: {}", loginDTO.getEmail(), e);
            throw new AuthenticationException("Email ou mot de passe incorrect");
        }
    }

    public AuthResponseDTO refreshToken() {
        Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();
        if (currentAuth == null || !currentAuth.isAuthenticated() || "anonymousUser".equals(currentAuth.getPrincipal())) {
            logger.error("Tentative de rafraîchissement du token sans authentification valide");
            throw new AuthenticationException("Session expirée. Veuillez vous reconnecter.");
        }

        String username = currentAuth.getName();
        User user = userRepository.findByEmail(username)
            .orElseThrow(() -> {
                logger.error("Utilisateur non trouvé pour l'email: {}", username);
                return new AuthenticationException("Session invalide. Veuillez vous reconnecter.");
            });

        logger.info("Rafraîchissement du token pour l'utilisateur: {}", username);
        String newToken = jwtTokenProvider.generateToken(currentAuth);
        logger.debug("Nouveau token généré");

        return new AuthResponseDTO(newToken, user);
    }

    public void logout() {
        logger.info("Déconnexion de l'utilisateur: {}", SecurityContextHolder.getContext().getAuthentication().getName());
        SecurityContextHolder.clearContext();
        logger.info("Contexte de sécurité nettoyé");
    }

    public Map<String, Object> checkAuthStatus() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Map<String, Object> status = new HashMap<>();
        
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            logger.debug("Utilisateur authentifié: {}", auth.getName());
            status.put("authenticated", true);
            status.put("username", auth.getName());
            status.put("roles", auth.getAuthorities());
        } else {
            logger.debug("Aucun utilisateur authentifié");
            status.put("authenticated", false);
        }
        
        return status;
    }

    public Map<String, Boolean> getPermissionsForEntity(String entity) {
        Map<String, Boolean> permissions = new HashMap<>();
        
        switch (entity) {
            case "user":
                permissions.put("canView", canManageUsers());
                permissions.put("canCreate", canManageUsers());
                permissions.put("canEdit", canManageUsers());
                permissions.put("canDelete", canManageUsers());
                break;
            case "project":
            case "collaborator":
                permissions.put("canView", canRead());
                permissions.put("canCreate", canWrite());
                permissions.put("canEdit", canWrite());
                permissions.put("canDelete", canWrite());
                break;
            case "assignment":
                permissions.put("canView", canRead());
                permissions.put("canCreate", canManageAssignments());
                permissions.put("canEdit", canManageAssignments());
                permissions.put("canDelete", canManageAssignments());
                break;
            default:
                permissions.put("canView", canRead());
                permissions.put("canCreate", false);
                permissions.put("canEdit", false);
                permissions.put("canDelete", false);
        }
        
        return permissions;
    }

    public Map<String, Boolean> checkUserAccess(Long userId) {
        Map<String, Boolean> access = new HashMap<>();
        access.put("canAccess", canAccessUser(userId));
        access.put("canModify", canModifyUser(userId));
        access.put("canDelete", canDeleteUser(userId));
        return access;
    }

    // Méthodes utilitaires pour les vérifications de permissions
    public boolean canRead() {
        return true; // Tout utilisateur authentifié peut lire
    }

    public boolean canWrite() {
        return hasRole(UserRole.ADMIN) || hasRole(UserRole.MANAGER);
    }

    public boolean canManageUsers() {
        return hasRole(UserRole.ADMIN);
    }

    public boolean canManageAssignments() {
        return hasRole(UserRole.ADMIN) || hasRole(UserRole.MANAGER);
    }

    public boolean canAccessUser(Long userId) {
        User currentUser = getCurrentUser();
        return currentUser != null && (hasRole(UserRole.ADMIN) || currentUser.getId().equals(userId));
    }

    public boolean canModifyUser(Long userId) {
        return hasRole(UserRole.ADMIN) || (getCurrentUser() != null && getCurrentUser().getId().equals(userId));
    }

    public boolean canDeleteUser(Long userId) {
        return hasRole(UserRole.ADMIN);
    }

    private boolean hasRole(UserRole role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return false;
        }
        
        return auth.getAuthorities().stream()
            .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role.name()));
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        return userRepository.findByEmail(auth.getName())
            .orElse(null);
    }

    public UserDTO getCurrentUserDTO() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            logger.debug("Récupération des informations de l'utilisateur courant: {}", auth.getName());
            User user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> {
                    logger.error("Utilisateur non trouvé pour l'email: {}", auth.getName());
                    return new IllegalStateException("Utilisateur non trouvé");
                });
            return UserDTO.fromEntity(user);
        }
        logger.debug("Aucun utilisateur courant trouvé");
        return null;
    }
} 