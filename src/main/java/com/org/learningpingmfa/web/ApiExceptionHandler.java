package com.org.learningpingmfa.web;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientResponseException;

/** Surfaces PingOne's own error body/status instead of a generic 500 when a downstream call fails. */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(RestClientResponseException.class)
    public ResponseEntity<String> handlePingOneError(RestClientResponseException ex) {
        HttpStatusCode status = ex.getStatusCode();
        return ResponseEntity.status(status).body(ex.getResponseBodyAsString());
    }
}
