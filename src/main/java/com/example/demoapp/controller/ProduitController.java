package com.example.demoapp.controller;

import com.example.demoapp.Iservices.ProduitService;
import com.example.demoapp.entities.Personne;
import com.example.demoapp.entities.Produit;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/produits")
@RequiredArgsConstructor
public class ProduitController {
    private final ProduitService produitService;

    @PostMapping
    @PreAuthorize("hasRole('client_admin')")
    public ResponseEntity<Produit> addProduit(@RequestBody Produit produit) {
        return ResponseEntity.status(HttpStatus.CREATED).body(produitService.addProduit(produit));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('client_admin')")
    public ResponseEntity<Produit> updateProduit( @PathVariable Long id, @RequestBody Produit produit) {
        return ResponseEntity.status(HttpStatus.CREATED).body(produitService.updateProduit(id, produit));

    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('client_admin')")
    public ResponseEntity<Void> deleteProduit(@PathVariable Long id) {
        produitService.deleteProduit(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasRole('client_admin') or hasRole('client_user') or hasRole('client_user_edit_statut')")

    public ResponseEntity<List<Produit>> getAllPersonnes() {
        return ResponseEntity.status(HttpStatus.CREATED).body(produitService.getAllProduits());

    }
}
