package com.org.learningpingmfa.web;

import com.org.learningpingmfa.client.PingOneMfaClient;
import com.org.learningpingmfa.dto.DeviceAuthenticationResponse;
import com.org.learningpingmfa.dto.OtpCheckRequest;
import com.org.learningpingmfa.dto.PingOneDeviceResponse;
import com.org.learningpingmfa.dto.RegisterDeviceRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Our own API surface — thin pass-through to {@link PingOneMfaClient}. Kept separate from the
 * PingOne wire DTOs deliberately: callers of this demo API never see PingOne's shape directly,
 * which is also where you'd add your own auth (session, JWT, whatever fronts this service) on
 * top of the worker-token calls this service makes to PingOne on the caller's behalf.
 */
@RestController
@RequestMapping("/api/mfa")
public class PingMfaController {

    private final PingOneMfaClient pingOneMfaClient;

    public PingMfaController(PingOneMfaClient pingOneMfaClient) {
        this.pingOneMfaClient = pingOneMfaClient;
    }

    @PostMapping("/users/{userId}/devices")
    public ResponseEntity<PingOneDeviceResponse> registerDevice(
            @PathVariable String userId, @Valid @RequestBody RegisterDeviceRequest request) {
        PingOneDeviceResponse device = pingOneMfaClient.registerDevice(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(device);
    }

    @GetMapping("/users/{userId}/devices")
    public List<PingOneDeviceResponse> listDevices(@PathVariable String userId) {
        return pingOneMfaClient.listDevices(userId);
    }

    @PostMapping("/users/{userId}/authentications")
    public ResponseEntity<DeviceAuthenticationResponse> initiateAuthentication(@PathVariable String userId) {
        DeviceAuthenticationResponse response = pingOneMfaClient.initiateDeviceAuthentication(userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/authentications/{authenticationId}/otp")
    public DeviceAuthenticationResponse checkOtp(
            @PathVariable String authenticationId, @Valid @RequestBody OtpCheckRequest request) {
        return pingOneMfaClient.checkOtp(authenticationId, request);
    }
}
