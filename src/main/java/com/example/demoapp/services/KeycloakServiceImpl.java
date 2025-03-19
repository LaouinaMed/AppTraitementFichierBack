package com.example.demoapp.services;

import com.example.demoapp.Iservices.KeycloakService;
import com.example.demoapp.entities.Personne;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class KeycloakServiceImpl implements KeycloakService {

    private final Keycloak keycloak;

    public String createUserInKeycloak(Personne personne) {

        try {

            UsersResource usersResource = keycloak.realm("appDemo").users();

            UserRepresentation user = new UserRepresentation();
            user.setUsername(personne.getEmail());
            user.setFirstName(personne.getPrenom());
            user.setLastName(personne.getNom());
            user.setEmail(personne.getEmail());
            user.setEnabled(true);


            user.setCredentials(Collections.emptyList());

            Response response = usersResource.create(user);

            if (response.getStatus() != 201) {
                throw new RuntimeException("Échec de la création de l'utilisateur Keycloak. Statut: " + response.getStatus());
            }

            String keycloakUserId = response.getLocation().getPath().split("/")[response.getLocation().getPath().split("/").length - 1];


            String clientId = "clientAppDemo";
            List<ClientRepresentation> clients = keycloak.realm("appDemo").clients().findByClientId(clientId);
            if (clients.isEmpty()) {
                throw new RuntimeException("Client introuvable : " + clientId);
            }
            String clientUuid = clients.get(0).getId(); // 🔥 Correction ici ! Utiliser getId() au lieu de getName()

            // Récupérer le rôle "user" du client
            List<RoleRepresentation> clientRoles = keycloak.realm("appDemo")
                    .clients()
                    .get(clientUuid)
                    .roles()
                    .list();

            RoleRepresentation userRole = clientRoles.stream()
                    .filter(role -> role.getName().equals("client_user")) // Vérifie si le rôle s'appelle "user"
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Rôle 'user' introuvable pour le client " + clientId));

            // Attribuer le rôle client "user" à l'utilisateur
            keycloak.realm("appDemo").users().get(keycloakUserId).roles().clientLevel(clientUuid).add(Collections.singletonList(userRole));

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

    public List<String> getUserRoles(String keycloakUserId) {
        try {
            /*
            UserRepresentation user = keycloak.realm("appDemo").users().get(keycloakUserId).toRepresentation();

            //List<RoleRepresentation> roles = keycloak.realm("appDemo").users().get(keycloakUserId).roles().clientLevel("clientAppDemo").listAll();

            //List<RoleRepresentation> roles = keycloak.realm("appDemo").users().get(keycloakUserId).roles().getAll().getRealmMappings();

            List<RoleRepresentation> roles = keycloak.realm("appDemo").users().get(keycloakUserId).roles().realmLevel().listAll();

            System.out.println("###############################################"); */

            String clientId = "clientAppDemo";

            List<ClientRepresentation> clients = keycloak.realm("appDemo").clients().findByClientId(clientId);
            if (clients.isEmpty()) {
                throw new RuntimeException("Client introuvable : " + clientId);
            }
            String clientUuid = clients.get(0).getId();


            List<RoleRepresentation> clientRoles = keycloak.realm("appDemo")
                    .users()
                    .get(keycloakUserId)
                    .roles()
                    .clientLevel(clientUuid)
                    .listAll();


            List<String> roleNames = new ArrayList<>();
            for (RoleRepresentation role : clientRoles) {
                roleNames.add(role.getName());
            }

            return roleNames;
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des rôles de l'utilisateur Keycloak", e);
            throw new RuntimeException("Erreur Keycloak : " + e.getMessage(), e);
        }
    }


}
