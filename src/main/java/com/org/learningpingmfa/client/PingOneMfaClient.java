package com.org.learningpingmfa.client;

import com.org.learningpingmfa.dto.DeviceAuthenticationResponse;
import com.org.learningpingmfa.dto.OtpCheckRequest;
import com.org.learningpingmfa.dto.PingOneDeviceRequest;
import com.org.learningpingmfa.dto.PingOneDeviceResponse;
import com.org.learningpingmfa.dto.RegisterDeviceRequest;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Thin wrapper over the two PingOne REST surfaces this demo touches — device pairing
 * (Management API) and device authentication / OTP check (Auth API). Every call rides the
 * worker client_credentials bearer token via the interceptor wired in
 * {@link com.org.learningpingmfa.config.PingOneClientConfig}.
 */
@Component
public class PingOneMfaClient {

    private final RestClient authClient;
    private final RestClient apiClient;

    public PingOneMfaClient(
            @Qualifier("pingOneAuthRestClient") RestClient authClient,
            @Qualifier("pingOneApiRestClient") RestClient apiClient) {
        this.authClient = authClient;
        this.apiClient = apiClient;
    }

    /** POST /environments/{envId}/users/{userId}/devices */
    public PingOneDeviceResponse registerDevice(String userId, RegisterDeviceRequest request) {
        PingOneDeviceRequest body = PingOneDeviceRequest.of(request.type(), request.target(), request.nickname());
        return apiClient.post()
                .uri("/users/{userId}/devices", userId)
                .body(body)
                .retrieve()
                .body(PingOneDeviceResponse.class);
    }

    /** GET /environments/{envId}/users/{userId}/devices */
    public List<PingOneDeviceResponse> listDevices(String userId) {
        PingOneDevicesPage page = apiClient.get()
                .uri("/users/{userId}/devices", userId)
                .retrieve()
                .body(PingOneDevicesPage.class);
        return page == null || page._embedded() == null ? List.of() : page._embedded().devices();
    }

    /** POST /{envId}/deviceAuthentications — kicks off a challenge against the user's active device. */
    public DeviceAuthenticationResponse initiateDeviceAuthentication(String userId) {
        return authClient.post()
                .uri("/deviceAuthentications")
                .body(new UserRef(new UserRef.Id(userId)))
                .retrieve()
                .body(DeviceAuthenticationResponse.class);
    }

    /** PUT /{envId}/deviceAuthentications/{id} with {"otp": "..."} — completes an OTP_REQUIRED challenge. */
    public DeviceAuthenticationResponse checkOtp(String deviceAuthenticationId, OtpCheckRequest otp) {
        return authClient.put()
                .uri("/deviceAuthentications/{id}", deviceAuthenticationId)
                .body(otp)
                .retrieve()
                .body(DeviceAuthenticationResponse.class);
    }

    private record UserRef(Id user) {
        private record Id(String id) {
        }
    }

    private record PingOneDevicesPage(Embedded _embedded) {
        private record Embedded(List<PingOneDeviceResponse> devices) {
        }
    }
}
