package com.org.learningpingmfa.dto;

import java.util.List;

public record PingOneDevicesPage(Embedded _embedded) {
    public record Embedded(List<PingOneDeviceResponse> devices) {
    }
}
