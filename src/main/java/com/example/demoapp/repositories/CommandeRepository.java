package com.example.demoapp.repositories;

import com.example.demoapp.entities.Commande;
import com.example.demoapp.entities.Personne;
import com.example.demoapp.entities.Produit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommandeRepository extends JpaRepository<Commande,Long> {
    List<Commande> findByPersonneKeycloakId(String keycloakUserId);

    List<Commande> findByPersonne(Personne personne);

    List<Commande> findByProduit(Produit produit);
    void deleteByPersonne(Personne personne);


}
