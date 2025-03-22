package com.example.demoapp.services;

import com.example.demoapp.Iservices.CommandeService;
import com.example.demoapp.entities.Commande;
import com.example.demoapp.entities.Personne;
import com.example.demoapp.entities.Produit;
import com.example.demoapp.enumeration.StatutCommande;
import com.example.demoapp.repositories.CommandeRepository;
import com.example.demoapp.repositories.PersonneRepository;
import com.example.demoapp.repositories.ProduitRepository;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class CommandeServiceImpl implements CommandeService {

    private final CommandeRepository commandeRepository;
    private final ProduitRepository produitRepository;
    private final PersonneRepository personneRepository;

    public CommandeServiceImpl(CommandeRepository commandeRepository, ProduitRepository produitRepository, PersonneRepository personneRepository) {
        this.commandeRepository = commandeRepository;
        this.produitRepository = produitRepository;
        this.personneRepository = personneRepository;
    }

    @Override
    public Commande ajouterCommande(String tel, String nom, String libellerProduit, Long quantite, String statut) {
        try {
            Personne personne = personneRepository.findByTel(tel)
                    .orElseThrow(() -> new IllegalArgumentException("Personne non trouvée avec le numéro de téléphone : " + tel));

            if (!personne.getNom().equalsIgnoreCase(nom)) {
                throw new IllegalArgumentException("Le nom fourni ne correspond pas à celui enregistré pour ce numéro de téléphone.");
            }

            Produit produit = produitRepository.findByLibeller(libellerProduit)
                    .orElseThrow(() -> new NoSuchElementException("Produit non trouvé avec le libeller : " + libellerProduit));

            if (quantite > produit.getQuantite()) {
                throw new IllegalArgumentException("La quantité demandée dépasse le stock disponible");
            }

            Long montant = produit.getPrix() * quantite;

            StatutCommande statutCommande = StatutCommande.valueOf(statut.toUpperCase());

            Commande commande = new Commande();
            commande.setProduit(produit);
            commande.setStatut(statutCommande);
            commande.setQuantite(quantite);
            commande.setMontant(montant);
            commande.setPersonne(personne);

            return commandeRepository.save(commande);

        } catch (IllegalArgumentException | NoSuchElementException e) {
            throw new RuntimeException("Erreur lors de l'ajout de la commande : " + e.getMessage());
        }
    }

    @Override

    public Commande modifierCommande(Long commandeId, String tel, String nom, String libellerProduit, Long quantite, String statut) {
        try {
            Commande commande = commandeRepository.findById(commandeId)
                    .orElseThrow(() -> new IllegalArgumentException("Commande non trouvée avec l'ID : " + commandeId));

            Personne personne = personneRepository.findByTel(tel)
                    .orElseThrow(() -> new IllegalArgumentException("Personne non trouvée avec le numéro de téléphone : " + tel));

            if (!personne.getNom().equalsIgnoreCase(nom)) {
                throw new IllegalArgumentException("Le nom fourni ne correspond pas à celui enregistré pour ce numéro de téléphone.");
            }

            Produit produit = produitRepository.findByLibeller(libellerProduit)
                    .orElseThrow(() -> new NoSuchElementException("Produit non trouvé avec le libeller : " + libellerProduit));

            if (quantite > produit.getQuantite()) {
                throw new IllegalArgumentException("La quantité demandée dépasse le stock disponible");
            }

            Long montant = produit.getPrix() * quantite;

            StatutCommande statutCommande = StatutCommande.valueOf(statut.toUpperCase());

            commande.setProduit(produit);
            commande.setStatut(statutCommande);
            commande.setQuantite(quantite);
            commande.setMontant(montant);
            commande.setPersonne(personne);

            return commandeRepository.save(commande);

        } catch (IllegalArgumentException | NoSuchElementException e) {
            throw new RuntimeException("Erreur lors de la modification de la commande : " + e.getMessage());
        }
    }

    @Override

    public void supprimerCommande(Long commandeId) {
        try {
            Commande commande = commandeRepository.findById(commandeId)
                    .orElseThrow(() -> new IllegalArgumentException("Commande non trouvée avec l'ID : " + commandeId));
            commandeRepository.delete(commande);

        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Erreur lors de la suppression de la commande : " + e.getMessage());
        }
    }

    @Override

    public List<Commande> getAllCommandes() {
        try {
            List<Commande> commandes = (List<Commande>) commandeRepository.findAll();
            return commandes;
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la récupération des commandes : " + e.getMessage());
        }
    }

    @Override

    public List<String> getStatutsDisponibles() {
        try {
            return List.of(StatutCommande.CONFIRMEE.name(), StatutCommande.REJETEE.name());
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la récupération des statuts disponibles : " + e.getMessage());
        }
    }
}