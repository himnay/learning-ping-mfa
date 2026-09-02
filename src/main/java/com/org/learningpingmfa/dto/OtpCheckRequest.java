package com.org.learningpingmfa.dto;

import jakarta.validation.constraints.NotBlank;

public record OtpCheckRequest(@NotBlank String otp) {
}
