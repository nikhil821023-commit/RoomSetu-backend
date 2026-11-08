package com.roomsetu.dto.auth;

import com.roomsetu.entity.enums.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OtpVerifyRequest {
    @NotBlank
    private String phone;

    @NotBlank
    private String otp;

    // Only required on first-time signup
    private String name;

    private Role role;
}