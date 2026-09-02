package com.org.learningpingmfa.dto;

/** MFA device types PingOne supports for server-driven pairing (native push devices need a pairing key instead). */
public enum DeviceType {
    SMS,
    VOICE,
    EMAIL,
    TOTP
}
