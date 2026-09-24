package com.org.learningpingmfa.client;

import com.org.learningpingmfa.dto.DeviceAuthenticationResponse;
import com.org.learningpingmfa.dto.OtpCheckRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Pins the wire contract of the PingOne device-authentication calls — PingOne picks the action
 * from the media type, so a wrong method or Content-Type is a runtime failure no unit test of the
 * controller would notice.
 */
class PingOneMfaClientTest {

    private static final String AUTH = "https://auth.pingone.test/env-1";

    private MockRestServiceServer server;
    private PingOneMfaClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder authBuilder = RestClient.builder().baseUrl(AUTH);
        server = MockRestServiceServer.bindTo(authBuilder).build();
        client = new PingOneMfaClient(authBuilder.build(), RestClient.builder().baseUrl("https://api.pingone.test").build());
    }

    @Test
    void initiateDeviceAuthentication_postsUserReferenceAsJson() {
        server.expect(requestTo(AUTH + "/deviceAuthentications"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("{\"user\":{\"id\":\"user-1\"}}"))
                .andRespond(withSuccess("{\"id\":\"da-1\",\"status\":\"OTP_REQUIRED\"}", MediaType.APPLICATION_JSON));

        DeviceAuthenticationResponse response = client.initiateDeviceAuthentication("user-1");

        assertThat(response.status()).isEqualTo("OTP_REQUIRED");
        server.verify();
    }

    @Test
    void checkOtp_postsWithOtpCheckMediaType() {
        server.expect(requestTo(AUTH + "/deviceAuthentications/da-1"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType("application/vnd.pingidentity.otp.check+json"))
                .andExpect(content().json("{\"otp\":\"555555\"}"))
                .andRespond(withSuccess("{\"id\":\"da-1\",\"status\":\"COMPLETED\"}", MediaType.APPLICATION_JSON));

        DeviceAuthenticationResponse response = client.checkOtp("da-1", new OtpCheckRequest("555555"));

        assertThat(response.status()).isEqualTo("COMPLETED");
        server.verify();
    }
}
