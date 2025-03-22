package com.example.demoapp.repositories;

import com.example.demoapp.entities.Commande;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommandeRepository extends JpaRepository<Commande,Long> {
    List<Commande> findByPersonneKeycloakId(String keycloakUserId);

}
