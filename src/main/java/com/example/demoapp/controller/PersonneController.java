package com.example.demoapp.controller;

import com.example.demoapp.entities.Personne;
import com.example.demoapp.Iservices.PersonneService;
import com.example.demoapp.services.KeycloakServiceImpl;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/personnes")
@RequiredArgsConstructor

public class PersonneController {

    private final PersonneService personneService;

    private final KeycloakServiceImpl keycloakService;


    @PostMapping("/upload")
    @PreAuthorize("hasRole('client_admin')")
    public ResponseEntity<Boolean> uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
        boolean isUploaded = personneService.saveFile(file);
        return ResponseEntity.status(isUploaded ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST).body(isUploaded);
    }


    @PostMapping
    @PreAuthorize("hasRole('client_admin')")
    public ResponseEntity<Personne> addPersonne( @RequestBody Personne personne) {
        return ResponseEntity.status(HttpStatus.CREATED).body(personneService.addPersonne(personne));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('client_admin')")
    public ResponseEntity<Personne> updatePersonne(@PathVariable Long id, @Nullable @RequestBody Personne personne) {
        return ResponseEntity.ok(personneService.updatePersonne(id, personne));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('client_admin')")
    public ResponseEntity<Void> deletePersonne(@PathVariable Long id) {
        personneService.deletePersonne(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasRole('client_admin') or hasRole('client_user')")

    public ResponseEntity<List<Personne>> getAllPersonnes() {
        return ResponseEntity.ok((List<Personne>) personneService.getAllPersonnes());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('client_admin')")
    public ResponseEntity<Personne> getPersonneById(@PathVariable Long id) {
        return personneService.getPersonneById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/roles/{id}")
    @PreAuthorize("hasRole('client_admin')")
    public ResponseEntity<List<String>> getUserRoles(@PathVariable String id) {

            List<String> roles = keycloakService.getUserRoles(id);
            return ResponseEntity.ok(roles);

    }

    @PutMapping("/roles/{id}")
    @PreAuthorize("hasRole('client_admin')")
    public ResponseEntity<Map<String, String>> assignRoleToUser(@PathVariable String id, @RequestParam String roleName) {
        try {
            keycloakService.assignRoleToUser(id, roleName);

            // Créez une Map pour structurer la réponse JSON
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Rôle " + roleName + " attribué à l'utilisateur " + id);

            // Retourne la réponse avec un statut HTTP 200 OK
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (RuntimeException e) {
            // Créez une Map pour l'erreur
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Erreur lors de l'attribution du rôle : " + e.getMessage());

            // Retourne une réponse avec un statut HTTP 400 BAD REQUEST
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }



    @GetMapping("/client-roles")
    @PreAuthorize("hasRole('client_admin')")
    public ResponseEntity<List<String>> getAllClientRoles() {
        try {
            List<String> roles = keycloakService.getAllClientRoles();
            return ResponseEntity.ok(roles);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.singletonList("Erreur : " + e.getMessage()));
        }
    }


    @DeleteMapping("/roles/{userId}")
    @PreAuthorize("hasRole('client_admin')")
    public ResponseEntity<Map<String, String>> removeRoleFromUser(@PathVariable String userId, @RequestParam String roleName) {
        try {
            // Suppression du rôle
            keycloakService.removeRoleFromUser(userId, roleName);

            // Créer un objet Map pour la réponse JSON
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Rôle supprimé avec succès");

            // Retourner la réponse avec un statut HTTP 200 OK
            return ResponseEntity.ok(response);  // Spring convertit automatiquement en JSON
        } catch (Exception e) {
            // Créer un objet Map pour la réponse d'erreur en JSON
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Erreur lors de la suppression du rôle : " + e.getMessage());

            // Retourner la réponse avec un statut HTTP 400 (BAD_REQUEST)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }



}
