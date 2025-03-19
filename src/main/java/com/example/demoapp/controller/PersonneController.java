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
import java.util.List;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/personnes")
@RequiredArgsConstructor

public class PersonneController {

    private final PersonneService personneService;

    private final KeycloakServiceImpl keycloakService;

    private static final Logger log = Logger.getLogger(PersonneController.class.getName());

    @PostMapping("/upload")
    @PreAuthorize("hasRole('client_admin')")
    public ResponseEntity<Boolean> uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
        boolean isUploaded = personneService.saveFile(file);
        return ResponseEntity.status(isUploaded ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST).body(isUploaded);
    }


    @PostMapping
    @PreAuthorize("hasRole('client_admin')")
    public ResponseEntity<Personne> addPersonne(@Valid @RequestBody Personne personne) {
        return ResponseEntity.status(HttpStatus.CREATED).body(personneService.addPersonne(personne));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('client_admin')")
    public ResponseEntity<Personne> updatePersonne(@Valid @PathVariable Long id, @Nullable @RequestBody Personne personne) {
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
    public ResponseEntity<String> assignRoleToUser(@PathVariable String id, @RequestParam String roleName) {
        try {
            keycloakService.assignRoleToUser(id, roleName);
            return ResponseEntity.ok("Rôle " + roleName + " attribué à l'utilisateur " + id);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

}
