package com.org.learningpingmfa.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor;
import org.springframework.web.client.RestClient;

/**
 * Wires a {@link RestClient} whose every request is stamped with a bearer token obtained
 * via client_credentials against the "pingone-worker" registration (see application.yaml).
 * Spring Security's client manager caches the token and refreshes it once it expires —
 * no manual token juggling needed in {@link com.org.learningpingmfa.client.PingOneMfaClient}.
 */
@Configuration
public class PingOneClientConfig {

    @Bean
    OAuth2AuthorizedClientManager pingOneAuthorizedClientManager(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientService authorizedClientService) {

        OAuth2AuthorizedClientProvider authorizedClientProvider = OAuth2AuthorizedClientProviderBuilder.builder()
                .clientCredentials()
                .build();

        AuthorizedClientServiceOAuth2AuthorizedClientManager manager =
                new AuthorizedClientServiceOAuth2AuthorizedClientManager(clientRegistrationRepository, authorizedClientService);
        manager.setAuthorizedClientProvider(authorizedClientProvider);
        return manager;
    }

    /**
     * deviceAuthentications lives on PingOne's auth domain ({@code auth.pingone.com/{envId}/...}),
     * separate from the Management API domain used for device CRUD — see {@link #pingOneApiRestClient}.
     */
    @Bean
    RestClient pingOneAuthRestClient(OAuth2AuthorizedClientManager pingOneAuthorizedClientManager, PingOneProperties properties) {
        return RestClient.builder()
                .baseUrl(properties.authBaseUrl() + "/" + properties.environmentId())
                .requestInterceptor(pingOneOAuth2Interceptor(pingOneAuthorizedClientManager))
                .build();
    }

    /** Device pairing/listing lives on the Management API domain ({@code api.pingone.com/environments/{envId}/...}). */
    @Bean
    RestClient pingOneApiRestClient(OAuth2AuthorizedClientManager pingOneAuthorizedClientManager, PingOneProperties properties) {
        return RestClient.builder()
                .baseUrl(properties.apiBaseUrl() + "/environments/" + properties.environmentId())
                .requestInterceptor(pingOneOAuth2Interceptor(pingOneAuthorizedClientManager))
                .build();
    }

    private OAuth2ClientHttpRequestInterceptor pingOneOAuth2Interceptor(OAuth2AuthorizedClientManager manager) {
        OAuth2ClientHttpRequestInterceptor interceptor = new OAuth2ClientHttpRequestInterceptor(manager);
        interceptor.setClientRegistrationIdResolver(request -> "pingone-worker");
        return interceptor;
    }
}
