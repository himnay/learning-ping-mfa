package com.org.learningpingmfa.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Trimmed view of PingOne's {@code deviceAuthentications} resource — verified shape (2026-09)
 * against POST {authBaseUrl}/{environmentId}/deviceAuthentications:
 * <pre>
 * {
 *   "id": "03e1897e-...",
 *   "status": "OTP_REQUIRED",
 *   "user": { "id": "788d4931-..." },
 *   "selectedDevice": { "id": "ea055ca3-..." }
 * }
 * </pre>
 * {@code status} is one of {@code OTP_REQUIRED}, {@code PUSH_CONFIRMATION_REQUIRED},
 * {@code DEVICE_SELECTION_REQUIRED}, or a terminal {@code COMPLETED}/{@code FAILED} once
 * the challenge resolves — see {@link com.org.learningpingmfa.dto.DeviceAuthenticationStatus}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record DeviceAuthenticationResponse(String id, String status, PingOneRef user, PingOneRef selectedDevice) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PingOneRef(String id) {
    }
}
