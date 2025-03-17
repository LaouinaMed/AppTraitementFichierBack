package com.example.demoapp.services;

import com.example.demoapp.entities.Personne;
import jakarta.ws.rs.core.Response;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@Slf4j
@RequiredArgsConstructor
public class KeycloakService {

    private final Keycloak keycloak;

    public String createUserInKeycloak(Personne personne) {

        try {


            // Accéder aux ressources des utilisateurs
            UsersResource usersResource = keycloak.realm("appDemo").users();

            UserRepresentation user = new UserRepresentation();
            user.setUsername(personne.getEmail());
            user.setFirstName(personne.getPrenom());
            user.setLastName(personne.getNom());
            user.setEmail(personne.getEmail());
            user.setEnabled(true);

            // Définir les informations de connexion (mot de passe)
            CredentialRepresentation passwordCredential = new CredentialRepresentation();
            passwordCredential.setType(CredentialRepresentation.PASSWORD);
            passwordCredential.setValue("123456789");
            user.setCredentials(Collections.singletonList(passwordCredential));

            Response response = usersResource.create(user);

            if (response.getStatus() != 201) {
                throw new RuntimeException("Échec de la création de l'utilisateur Keycloak. Statut: " + response.getStatus());
            }

            String keycloakUserId = response.getLocation().getPath().split("/")[response.getLocation().getPath().split("/").length - 1];

            return keycloakUserId;

        } catch (Exception e) {
            log.error("Erreur lors de l'ajout de l'utilisateur Keycloak", e);
            throw new RuntimeException("Erreur Keycloak : " + e.getMessage(), e);
        }
    }
}
