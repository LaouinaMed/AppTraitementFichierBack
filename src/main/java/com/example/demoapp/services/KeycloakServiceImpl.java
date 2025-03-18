package com.example.demoapp.services;

import com.example.demoapp.Iservices.KeycloakService;
import com.example.demoapp.entities.Personne;
import jakarta.ws.rs.core.Response;
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
public class KeycloakServiceImpl implements KeycloakService {

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
/*
            // Définir les informations de connexion (mot de passe)
            CredentialRepresentation passwordCredential = new CredentialRepresentation();
            passwordCredential.setType(CredentialRepresentation.PASSWORD);
            passwordCredential.setValue("123456789");
            user.setCredentials(Collections.singletonList(passwordCredential));
*/

            user.setCredentials(Collections.emptyList());  // Pas de mot de passe initial
            //user.setRequiredActions(Collections.singletonList("UPDATE_PASSWORD"));


            Response response = usersResource.create(user);

            if (response.getStatus() != 201) {
                throw new RuntimeException("Échec de la création de l'utilisateur Keycloak. Statut: " + response.getStatus());
            }

            String keycloakUserId = response.getLocation().getPath().split("/")[response.getLocation().getPath().split("/").length - 1];

            keycloak.realm("appDemo")
                    .users()
                    .get(keycloakUserId)
                    .executeActionsEmail(Collections.singletonList("UPDATE_PASSWORD"));

            return keycloakUserId;

        } catch (Exception e) {
            log.error("Erreur lors de l'ajout de l'utilisateur Keycloak", e);
            throw new RuntimeException("Erreur Keycloak : " + e.getMessage(), e);
        }
    }


    public void updateUserInKeycloak(String keycloakUserId, Personne personne) {
        try {
            UserRepresentation user = keycloak.realm("appDemo").users().get(keycloakUserId).toRepresentation();

            user.setFirstName(personne.getPrenom());
            user.setLastName(personne.getNom());
            user.setEmail(personne.getEmail());

            keycloak.realm("appDemo").users().get(keycloakUserId).update(user);

        } catch (Exception e) {
            log.error("Erreur lors de la mise à jour de l'utilisateur Keycloak", e);
            throw new RuntimeException("Erreur Keycloak : " + e.getMessage(), e);
        }
    }


    public void deleteUserInKeycloak(String keycloakUserId) {
        try {
            keycloak.realm("appDemo").users().get(keycloakUserId).remove();

        } catch (Exception e) {
            log.error("Erreur lors de la suppression de l'utilisateur Keycloak", e);
            throw new RuntimeException("Erreur Keycloak : " + e.getMessage(), e);
        }
    }
}
