package com.org.learningpingmfa.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Trimmed view of PingOne's device resource — only the fields this demo actually uses. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PingOneDeviceResponse(String id, String type, String status, String nickname) {
}
