package com.staffing.dto;

import com.staffing.model.User;
import lombok.Data;

@Data
public class AuthResponseDTO {
    private String token;
    private UserDTO user;

    public AuthResponseDTO(String token, User user) {
        this.token = token;
        this.user = UserDTO.fromEntity(user);
    }
} 