package com.org.learningpingmfa.dto;

/**
 * Values of {@link DeviceAuthenticationResponse#status()}. {@code OTP_REQUIRED},
 * {@code PUSH_CONFIRMATION_REQUIRED} and {@code DEVICE_SELECTION_REQUIRED} are confirmed
 * against PingOne's live API docs (2026-09); {@code COMPLETED}/{@code FAILED} are the commonly
 * documented terminal values but weren't independently re-verified here — confirm against your
 * tenant before branching production logic on them.
 */
public enum DeviceAuthenticationStatus {
    /** A code sent to the selected device (SMS/EMAIL/VOICE/TOTP) must be submitted to {@code /otp}. */
    OTP_REQUIRED,
    /** A push notification is awaiting the user's approve/deny tap on their device. */
    PUSH_CONFIRMATION_REQUIRED,
    /** The user has more than one active device; the caller must select one first. */
    DEVICE_SELECTION_REQUIRED,
    /** Challenge satisfied. */
    COMPLETED,
    /** Challenge failed (wrong OTP too many times, denied push, expired). */
    FAILED
}
