package com.example.backendprojet.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
public class KeycloakConfig {

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {

        ClientRegistration registration = ClientRegistration.withRegistrationId("keycloak")
                .clientId("projet-client")
                .clientSecret("JPxawKadfqFXsx7XwqL0RyjLzNPZw1he")
                .scope("openid")
                .authorizationUri("http://localhost:8080/realms/projet/protocol/openid-connect/auth")
                .tokenUri("http://localhost:8080/realms/projet/protocol/openid-connect/token")
                .userInfoUri("http://localhost:8080/realms/projet/protocol/openid-connect/userinfo")
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .userNameAttributeName(IdTokenClaimNames.SUB)
                .clientName("Keycloak")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .build();

        return new InMemoryClientRegistrationRepository(registration);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .cors(withDefaults())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/users/**", "/api/processus/**").permitAll() // 🔹 Ajouté ici
                        .anyRequest().authenticated()
                )
                .oauth2Login(withDefaults());

        return http.build();
    }
}