package com.example.demoapp.repositories;

import com.example.demoapp.entities.Commande;
import com.example.demoapp.entities.Produit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProduitRepository extends JpaRepository<Produit,Long> {

    Optional<Produit> findByLibeller(String libeller);
}
