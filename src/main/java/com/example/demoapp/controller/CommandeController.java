package com.example.demoapp.controller;

import com.example.demoapp.entities.Commande;
import com.example.demoapp.services.CommandeServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/commandes")
public class CommandeController {

    private CommandeServiceImpl commandeService;

    public CommandeController(CommandeServiceImpl commandeService) {
        this.commandeService = commandeService;
    }

    @PostMapping
    @PreAuthorize("hasRole('client_admin')")

    public ResponseEntity<Commande> ajouterCommande(@RequestBody Commande commande) {
            Commande nouvelleCommande = commandeService.ajouterCommande(commande);
            return ResponseEntity.status(HttpStatus.CREATED).body(nouvelleCommande);
    }

    @PutMapping("/{commandeId}")
    @PreAuthorize("hasRole('client_admin') or hasRole('client_user_edit_statut')")
    public ResponseEntity<Commande> modifierCommande(@PathVariable Long commandeId, @RequestBody Commande commande) {

        Commande updatedCommande = commandeService.modifierCommande(commandeId,commande );
        return ResponseEntity.status(HttpStatus.OK).body(updatedCommande);

    }

    @DeleteMapping("/{commandeId}")
    @PreAuthorize("hasRole('client_admin')")

    public ResponseEntity<String> supprimerCommande(@PathVariable Long commandeId) {
        commandeService.supprimerCommande(commandeId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasRole('client_admin') or hasRole('client_user') or hasRole('client_user_edit_statut')")
    public ResponseEntity<List<Commande>> getAllCommandes() {

            // Récupérer l'ID de l'utilisateur connecté depuis le contexte de sécurité
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String keycloakUserId = authentication.getName();  // Cela suppose que le nom d'utilisateur dans Keycloak est l'ID de l'utilisateur


            List<Commande> commandes = commandeService.getAllCommandes(keycloakUserId);

            return ResponseEntity.status(HttpStatus.OK).body(commandes);
    }

    @GetMapping("/statuts")
    @PreAuthorize("hasRole('client_admin') or hasRole('client_user_edit_statut')")

    public ResponseEntity<List<String>> getStatutsDisponibles() {
        List<String> statuts = commandeService.getStatutsDisponibles();
        return ResponseEntity.status(HttpStatus.OK).body(statuts);
    }
}
