package com.org.learningpingmfa.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

/**
 * Maps PingOne failures onto our API. Problems with the caller's request (400 wrong OTP, 404 unknown
 * user/device, 409, 429 …) are surfaced with PingOne's own status and body. Failures of this service
 * itself are not: a 401/403 means PingOne rejected <em>our worker credentials</em> (bad secret, missing
 * role) and 5xx is an upstream outage — relaying those would tell the caller that <em>they</em> are
 * unauthenticated and leak PingOne's error body, so they become a 502 and the details go to the log.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(RestClientResponseException.class)
    public ResponseEntity<?> handlePingOneError(RestClientResponseException ex) {
        HttpStatusCode status = ex.getStatusCode();
        if (status.value() == 401 || status.value() == 403 || status.is5xxServerError()) {
            log.error("PingOne call failed with HTTP {}: {}", status.value(), ex.getResponseBodyAsString());
            String detail = status.is5xxServerError()
                    ? "PingOne is unavailable (HTTP " + status.value() + ")"
                    : "PingOne rejected this service's worker credentials (HTTP " + status.value() + ")";
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(ProblemDetail.forStatusAndDetail(HttpStatus.BAD_GATEWAY, detail));
        }
        MediaType contentType = ex.getResponseHeaders() == null ? null : ex.getResponseHeaders().getContentType();
        return ResponseEntity.status(status)
                .contentType(contentType != null ? contentType : MediaType.APPLICATION_JSON)
                .body(ex.getResponseBodyAsString());
    }

    /** Connect/read timeout (see PingOneClientConfig) or connection failure — no response from PingOne at all. */
    @ExceptionHandler(ResourceAccessException.class)
    public ProblemDetail handlePingOneUnreachable(ResourceAccessException ex) {
        log.error("PingOne unreachable: {}", ex.getMessage());
        return ProblemDetail.forStatusAndDetail(HttpStatus.GATEWAY_TIMEOUT, "PingOne did not respond");
    }
}
