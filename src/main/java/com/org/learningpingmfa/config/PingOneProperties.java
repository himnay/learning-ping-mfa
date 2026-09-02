package com.org.learningpingmfa.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ping")
public record PingOneProperties(String environmentId, String authBaseUrl, String apiBaseUrl) {
}
