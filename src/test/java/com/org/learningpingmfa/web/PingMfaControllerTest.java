package com.org.learningpingmfa.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.org.learningpingmfa.client.PingOneMfaClient;
import com.org.learningpingmfa.config.SecurityConfig;
import com.org.learningpingmfa.dto.DeviceAuthenticationResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.nio.charset.StandardCharsets;

/**
 * @Import(SecurityConfig.class): @WebMvcTest slices exclude plain @Configuration beans by
 * default, so without this the sliced context falls back to Boot's auto-configured OAuth2
 * *login* filter chain (see SecurityConfig's javadoc) — which fails to build inside the slice.
 * Importing our own permit-all chain here mirrors what component scanning gives the real app.
 */
@WebMvcTest(PingMfaController.class)
@Import(SecurityConfig.class)
class PingMfaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PingOneMfaClient pingOneMfaClient;

    @Test
    void initiateAuthentication_returnsPingOneChallengeStatus() throws Exception {
        when(pingOneMfaClient.initiateDeviceAuthentication(eq("user-123")))
                .thenReturn(new DeviceAuthenticationResponse(
                        "auth-1", "OTP_REQUIRED",
                        new DeviceAuthenticationResponse.PingOneRef("user-123"),
                        new DeviceAuthenticationResponse.PingOneRef("device-1")));

        mockMvc.perform(post("/api/mfa/users/user-123/authentications"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("auth-1"))
                .andExpect(jsonPath("$.status").value("OTP_REQUIRED"));
    }

    @Test
    void pingOneRejectingTheWorkerTokenIsABadGatewayNotTheCallersAuthProblem() throws Exception {
        when(pingOneMfaClient.initiateDeviceAuthentication(eq("user-123")))
                .thenThrow(HttpClientErrorException.create(HttpStatus.UNAUTHORIZED, "Unauthorized", HttpHeaders.EMPTY,
                        "{\"code\":\"INVALID_TOKEN\"}".getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));

        mockMvc.perform(post("/api/mfa/users/user-123/authentications"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.detail").value("PingOne rejected this service's worker credentials (HTTP 401)"));
    }

    @Test
    void pingOneOutageIsABadGateway() throws Exception {
        when(pingOneMfaClient.initiateDeviceAuthentication(eq("user-123")))
                .thenThrow(HttpServerErrorException.create(HttpStatus.SERVICE_UNAVAILABLE, "Unavailable", HttpHeaders.EMPTY,
                        new byte[0], StandardCharsets.UTF_8));

        mockMvc.perform(post("/api/mfa/users/user-123/authentications"))
                .andExpect(status().isBadGateway());
    }

    @Test
    void pingOneRejectingTheCallersInputPassesThrough() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        when(pingOneMfaClient.checkOtp(eq("auth-1"), any()))
                .thenThrow(HttpClientErrorException.create(HttpStatus.BAD_REQUEST, "Bad Request", headers,
                        "{\"code\":\"INVALID_DATA\"}".getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));

        mockMvc.perform(post("/api/mfa/authentications/auth-1/otp")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"otp\":\"123456\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("INVALID_DATA"));
    }

    @Test
    void nonNumericOtpIsRejectedBeforeCallingPingOne() throws Exception {
        mockMvc.perform(post("/api/mfa/authentications/auth-1/otp")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"otp\":\"12ab\"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(pingOneMfaClient);
    }
}
