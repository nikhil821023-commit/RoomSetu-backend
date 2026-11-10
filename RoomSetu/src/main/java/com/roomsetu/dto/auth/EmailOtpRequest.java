package com.roomsetu.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class EmailOtpRequest {
    @NotBlank @Email @Size(max = 150)
    private String email;
}