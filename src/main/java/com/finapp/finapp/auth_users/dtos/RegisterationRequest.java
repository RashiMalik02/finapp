package com.finapp.finapp.auth_users.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class RegisterationRequest {
    @NotBlank(message = "FirstName is required")
    private String firstName;

    private String lastName;

    private String phoneNumber;

    @Email
    @NotBlank(message = "Email is required")
    private String email;

    private List<String> roles;

    @NotBlank(message = "Password is required")
    private String password;
}
