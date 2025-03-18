package com.example.demoapp.Iservices;

import com.example.demoapp.entities.Personne;

public interface KeycloakService {
    public String createUserInKeycloak(Personne personne) ;

    public void updateUserInKeycloak(String keycloakUserId, Personne personne);

    public void deleteUserInKeycloak(String keycloakUserId);
}
