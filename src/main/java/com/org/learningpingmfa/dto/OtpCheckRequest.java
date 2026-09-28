package com.org.learningpingmfa.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * Body of our OTP check. PingOne one-time passcodes are numeric (6 digits by default), so anything
 * else is rejected with a 400 before a worker-token call is spent on it.
 */
public record OtpCheckRequest(@NotNull @Pattern(regexp = "\\d{6,10}", message = "must be 6 to 10 digits") String otp) {
}
