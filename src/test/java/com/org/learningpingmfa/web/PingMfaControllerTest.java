package com.org.learningpingmfa.web;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.org.learningpingmfa.client.PingOneMfaClient;
import com.org.learningpingmfa.config.SecurityConfig;
import com.org.learningpingmfa.dto.DeviceAuthenticationResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

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
}
