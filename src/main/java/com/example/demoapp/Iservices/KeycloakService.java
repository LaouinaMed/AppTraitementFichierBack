package com.example.demoapp.Iservices;

import com.example.demoapp.entities.Personne;

import java.util.List;
import java.util.Optional;

public interface KeycloakService {
    public String createUserInKeycloak(Personne personne) ;

    public void updateUserInKeycloak(String keycloakUserId, Personne personne);

    public void deleteUserInKeycloak(String keycloakUserId);

    List<String> getUserRoles(String keycloakUserId);

}
