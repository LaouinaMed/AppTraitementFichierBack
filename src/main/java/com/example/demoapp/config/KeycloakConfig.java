package com.example.demoapp.config;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeycloakConfig {


    private static final String KEYCLOAK_SERVER_URL = "http://localhost:8080";
    private static final String REALM = "appDemo";
    private static final String CLIENT_ID = "clientAppDemo";
    private static final String USERNAME = "simed.laouina@gmail.com";
    private static final String PASSWORD = "user";

    @Bean
    public Keycloak keycloakInstance() {
        return KeycloakBuilder.builder()
                .serverUrl(KEYCLOAK_SERVER_URL)
                .realm(REALM)
                .clientId(CLIENT_ID)
                .username(USERNAME)
                .password(PASSWORD)
                .build();

    }
}