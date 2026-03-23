package com.netflix.backend.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record VerifyRequest(@Email @NotBlank String email,@NotBlank Integer otp) {
}