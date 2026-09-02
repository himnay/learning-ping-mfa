package com.org.learningpingmfa.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Our API's input shape. {@code target} carries the phone number (SMS/VOICE) or email address
 * (EMAIL); ignored for TOTP, which PingOne provisions as a secret/QR instead.
 */
public record RegisterDeviceRequest(@NotNull DeviceType type, String target, String nickname) {
}
