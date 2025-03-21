package com.example.demoapp.controller;

import com.example.demoapp.entities.Commande;
import com.example.demoapp.services.CommandeServiceImpl;
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
            Commande nouvelleCommande = commandeService.ajouterCommande(
                    commande.getPersonne().getTel(),
                    commande.getPersonne().getNom(),
                    commande.getProduit().getLibeller(),
                    commande.getQuantite(),
                    commande.getStatut().name()
            );
            return ResponseEntity.ok(nouvelleCommande);
    }


    // Endpoint pour modifier une commande
    @PutMapping("/{commandeId}")
    @PreAuthorize("hasRole('client_admin')")

    public ResponseEntity<Commande> modifierCommande(@PathVariable Long commandeId, @RequestBody Commande commande) {

        Commande updatedCommande = commandeService.modifierCommande(
                commandeId,
                commande.getPersonne().getTel(),
                commande.getPersonne().getNom(),
                commande.getProduit().getLibeller(),
                commande.getQuantite(),
                commande.getStatut().name()
        );
        return ResponseEntity.ok(updatedCommande);

    }


    @DeleteMapping("/{commandeId}")
    @PreAuthorize("hasRole('client_admin')")

    public ResponseEntity<String> supprimerCommande(@PathVariable Long commandeId) {
        commandeService.supprimerCommande(commandeId);
        return ResponseEntity.noContent().build();
    }

    // Endpoint pour afficher toutes les commandes
    @GetMapping
    @PreAuthorize("hasRole('client_admin') or hasRole('client_user')")
    public ResponseEntity<List<Commande>> getAllCommandes() {

            // Récupérer l'ID de l'utilisateur connecté depuis le contexte de sécurité
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String keycloakUserId = authentication.getName();  // Cela suppose que le nom d'utilisateur dans Keycloak est l'ID de l'utilisateur


            List<Commande> commandes = commandeService.getAllCommandes(keycloakUserId);

            return ResponseEntity.ok(commandes);
    }

    // Endpoint pour afficher les statuts disponibles (confirmé/rejeté)
    @GetMapping("/statuts")
    @PreAuthorize("hasRole('client_admin')")

    public ResponseEntity<List<String>> getStatutsDisponibles() {
        List<String> statuts = commandeService.getStatutsDisponibles();
        return ResponseEntity.ok(statuts);
    }
}
