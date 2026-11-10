package com.roomsetu.dto.auth;

import com.roomsetu.entity.enums.Role;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EmailOtpVerifyRequest {
    @NotBlank
    private String email;

    @NotBlank
    private String otp;

    // Only required on first-time signup
    private String name;

    private Role role;
}