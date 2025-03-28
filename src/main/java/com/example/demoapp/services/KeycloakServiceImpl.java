package com.example.demoapp.services;

import com.example.demoapp.Iservices.KeycloakService;
import com.example.demoapp.entities.Personne;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class KeycloakServiceImpl implements KeycloakService {

    private final Keycloak keycloak;
    String clientId = "clientAppDemo";
    String nameRealm = "appDemo";



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
                throw new RuntimeException("Echec de la creation de utilisation Keycloak. Statut: " + response.getStatus());
            }

            String keycloakUserId = response.getLocation().getPath().split("/")[response.getLocation().getPath().split("/").length - 1];


            List<ClientRepresentation> clients =  keycloak.realm(nameRealm).clients().findByClientId(clientId);

            if (clients.isEmpty()) {
                throw new RuntimeException("Client introuvable : " + clientId);
            }
            String clientUuid = clients.get(0).getId();

            List<RoleRepresentation> clientRoles = keycloak.realm("appDemo")
                    .clients()
                    .get(clientUuid)
                    .roles()
                    .list();

            RoleRepresentation userRole = clientRoles.stream()
                    .filter(role -> role.getName().equals("client_user"))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Rôle 'user' introuvable pour le client " + clientId));

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
            List<ClientRepresentation> clients =  keycloak.realm(nameRealm).clients().findByClientId(clientId);

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


    public void assignRoleToUser(String keycloakUserId, String roleName) {
        try {

            List<ClientRepresentation> clients =  keycloak.realm(nameRealm).clients().findByClientId(clientId);


            if (clients.isEmpty()) {
                throw new RuntimeException("Client introuvable : " + clientId);
            }
            String clientUuid = clients.get(0).getId();

            List<RoleRepresentation> clientRoles = keycloak.realm("appDemo")
                    .clients()
                    .get(clientUuid)
                    .roles()
                    .list();

            Optional<RoleRepresentation> roleOptional = clientRoles.stream()
                    .filter(role -> role.getName().equals(roleName))
                    .findFirst();

            if (!roleOptional.isPresent()) {
                throw new RuntimeException("Le rôle " + roleName + " n'existe pas dans le client " + clientId);
            }

            RoleRepresentation roleToAssign = roleOptional.get();

            keycloak.realm("appDemo")
                    .users()
                    .get(keycloakUserId)
                    .roles()
                    .clientLevel(clientUuid)
                    .add(Collections.singletonList(roleToAssign));

            log.info("Role " + roleName + " attribué à l'utilisateur " + keycloakUserId);
        } catch (Exception e) {
            log.error("Erreur lors de l'attribution du rôle à l'utilisateur Keycloak", e);
            throw new RuntimeException("Erreur Keycloak : " + e.getMessage(), e);
        }
    }

    public List<String> getAllClientRoles() {
        try {
            List<ClientRepresentation> clients =  keycloak.realm(nameRealm).clients().findByClientId(clientId);

            if (clients.isEmpty()) {
                throw new RuntimeException("Client introuvable : " + clientId);
            }
            String clientUuid = clients.get(0).getId();

            List<RoleRepresentation> clientRoles = keycloak.realm("appDemo")
                    .clients()
                    .get(clientUuid)
                    .roles()
                    .list();

            List<String> roleNames = new ArrayList<>();
            for (RoleRepresentation role : clientRoles) {
                if(role.getName().equals("client_admin") ){
                    continue;
                }
                roleNames.add(role.getName());
            }

            return roleNames;
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des rôles du client Keycloak", e);
            throw new RuntimeException("Erreur Keycloak : " + e.getMessage(), e);
        }
    }

    public void removeRoleFromUser(String userId, String roleName) {
        try {
            List<ClientRepresentation> clients =  keycloak.realm(nameRealm).clients().findByClientId(clientId);

            if (clients.isEmpty()) {
                throw new RuntimeException("Client introuvable : " + clientId);
            }
            String clientUuid = clients.get(0).getId();

            List<RoleRepresentation> clientRoles = keycloak.realm("appDemo")
                    .clients()
                    .get(clientUuid)
                    .roles()
                    .list();

            RoleRepresentation roleToRemove = clientRoles.stream()
                    .filter(role -> role.getName().equals(roleName))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Rôle non trouvé"));

            keycloak.realm("appDemo")
                    .users()
                    .get(userId)
                    .roles()
                    .clientLevel(clientUuid)
                    .remove(Collections.singletonList(roleToRemove));
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la suppression du rôle : " + e.getMessage(), e);
        }
    }
}
