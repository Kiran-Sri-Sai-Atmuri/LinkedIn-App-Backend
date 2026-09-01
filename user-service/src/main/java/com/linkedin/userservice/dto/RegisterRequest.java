package com.linkedin.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    @NotBlank(message = "Email is Required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password id Required")
    @Size(min = 6,message = "Password must be at least 6 characters")
    private String password;


    @NotBlank(message = "First name id Required")
    private String firstName;
    @NotBlank(message = "Last name id Required")
    private String lastName;
    private String headline;
    private String location;
}
