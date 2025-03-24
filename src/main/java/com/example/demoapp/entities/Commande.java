package com.example.demoapp.entities;

import com.example.demoapp.enumeration.StatutCommande;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Commande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Enumerated(EnumType.STRING)
    private StatutCommande statut;
    private Long quantite;
    private Long montant;
    @ManyToOne
    private Personne personne;

    @ManyToOne
    private Produit produit;


}
