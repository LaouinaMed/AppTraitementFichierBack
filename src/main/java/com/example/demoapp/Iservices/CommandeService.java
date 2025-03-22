package com.example.demoapp.Iservices;

import com.example.demoapp.entities.Commande;

import java.util.List;

public interface CommandeService {

    public Commande ajouterCommande(String tel, String nom, String libellerProduit, Long quantite, String statut) ;
    public List<String> getStatutsDisponibles() ;
    public void supprimerCommande(Long commandeId) ;
    public Commande modifierCommande(Long commandeId, String tel, String nom, String libellerProduit, Long quantite, String statut) ;

    public List<Commande> getAllCommandes() ;

    }

