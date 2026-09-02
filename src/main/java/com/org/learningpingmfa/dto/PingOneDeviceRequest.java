package com.org.learningpingmfa.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Wire shape PingOne's Devices API expects: {@code POST /environments/{envId}/users/{userId}/devices}.
 * Field names ("phone"/"email") per the PingOne Platform Devices API — distinct from the
 * separate PingID product's SDK, which uses "phoneNumber". Verify against your tenant's live
 * API reference (Devices resource) before relying on this in anything beyond a learning demo.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PingOneDeviceRequest(String type, String phone, String email, String nickname) {

    public static PingOneDeviceRequest of(DeviceType type, String target, String nickname) {
        return switch (type) {
            case SMS, VOICE -> new PingOneDeviceRequest(type.name(), target, null, nickname);
            case EMAIL -> new PingOneDeviceRequest(type.name(), null, target, nickname);
            case TOTP -> new PingOneDeviceRequest(type.name(), null, null, nickname);
        };
    }
}
