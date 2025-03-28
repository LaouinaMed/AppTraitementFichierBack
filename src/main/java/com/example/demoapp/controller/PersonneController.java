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


    @PostMapping
    @PreAuthorize("hasRole('client_admin')")
    public ResponseEntity<Personne> addPersonne( @RequestBody Personne personne) {
        return ResponseEntity.status(HttpStatus.CREATED).body(personneService.addPersonne(personne));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('client_admin')")
    public ResponseEntity<Personne> updatePersonne(@PathVariable Long id, @RequestBody Personne personne) {
        return ResponseEntity.status(HttpStatus.CREATED).body(personneService.updatePersonne(id, personne));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('client_admin')")
    public ResponseEntity<Void> deletePersonne(@PathVariable Long id) {
        personneService.deletePersonne(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasRole('client_admin')")
    public ResponseEntity<List<Personne>> getAllPersonnes() {
        return ResponseEntity.ok((List<Personne>) personneService.getAllPersonnes());
    }

    @GetMapping("/roles/{id}")
    @PreAuthorize("hasRole('client_admin')")
    public ResponseEntity<List<String>> getUserRoles(@PathVariable String id) {

            List<String> roles = keycloakService.getUserRoles(id);
            return ResponseEntity.status(HttpStatus.OK).body(roles);

    }

    @PutMapping("/roles/{id}")
    @PreAuthorize("hasRole('client_admin')")
    public ResponseEntity<Map<String, String>> assignRoleToUser(@PathVariable String id, @RequestParam String roleName) {
        keycloakService.assignRoleToUser(id, roleName);

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Rôle " + roleName + " attribué à l'utilisateur " + id);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }



    @GetMapping("/client-roles")
    @PreAuthorize("hasRole('client_admin')")
    public ResponseEntity<List<String>> getAllClientRoles() {

            List<String> roles = keycloakService.getAllClientRoles();
            return ResponseEntity.status(HttpStatus.OK).body(roles);

    }


    @DeleteMapping("/roles/{userId}")
    @PreAuthorize("hasRole('client_admin')")
    public ResponseEntity<Map<String, String>> removeRoleFromUser(@PathVariable String userId, @RequestParam String roleName) {

            keycloakService.removeRoleFromUser(userId, roleName);

            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Rôle supprimé avec succès");

        return ResponseEntity.status(HttpStatus.OK).body(response);

    }


    @GetMapping("/{id}")
    @PreAuthorize("hasRole('client_admin')")
    public ResponseEntity<Personne> getPersonneById(@PathVariable Long id) {
        return personneService.getPersonneById(id)
                .map(personne -> ResponseEntity.ok(personne))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }



}
