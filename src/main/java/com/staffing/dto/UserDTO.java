package com.staffing.dto;

import com.staffing.model.User;
import com.staffing.model.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserDTO {
    private Long id;

    @NotBlank(message = "Le prénom est requis")
    private String firstName;

    @NotBlank(message = "Le nom est requis")
    private String lastName;

    @NotBlank(message = "L'email est requis")
    @Email(message = "L'email doit être valide")
    private String email;

    @Pattern(regexp = "^[+]?[(]?[0-9]{1,4}[)]?[-\\s./0-9]*$", message = "Le numéro de téléphone n'est pas valide")
    private String phone;

    @NotBlank(message = "Le poste est requis")
    private String position;

    private String password;

    @NotNull(message = "Le rôle est requis")
    private UserRole role;

    // Champs enrichis pour l'affichage
    private String roleLabel;
    private boolean canDelete;
    private boolean canManageUsers;

    public static UserDTO fromEntity(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setPosition(user.getPosition());
        dto.setRole(user.getRole());

        // Enrichir avec les informations du rôle
        dto.setRoleLabel(user.getRole().getLabel());
        dto.setCanDelete(user.getRole().canDelete());
        dto.setCanManageUsers(user.getRole().canManageUsers());

        return dto;
    }

    public User toEntity() {
        User user = new User();
        user.setId(this.id);
        user.setFirstName(this.firstName);
        user.setLastName(this.lastName);
        user.setEmail(this.email);
        user.setPhone(this.phone);
        user.setPosition(this.position);
        user.setRole(this.role);
        if (this.password != null && !this.password.isEmpty()) {
            user.setPassword(this.password);
        }
        return user;
    }
}