package com.example.demoapp.services;

import com.example.demoapp.Iservices.CommandeService;
import com.example.demoapp.Iservices.KeycloakService;
import com.example.demoapp.entities.Commande;
import com.example.demoapp.entities.Personne;
import com.example.demoapp.entities.Produit;
import com.example.demoapp.enumeration.StatutCommande;
import com.example.demoapp.repositories.CommandeRepository;
import com.example.demoapp.repositories.PersonneRepository;
import com.example.demoapp.repositories.ProduitRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

@Slf4j
@Service
public class CommandeServiceImpl implements CommandeService {

    private final CommandeRepository commandeRepository;
    private final ProduitRepository produitRepository;
    private final PersonneRepository personneRepository;
    private final KeycloakService keycloakService;

    private static final String STORAGE_DIRECTORY = "C:\\Users\\simed\\Desktop\\ReaderBatch";

    //private static final Logger log = Logger.getLogger(PersonneServiceImpl.class.getName());


    public CommandeServiceImpl(CommandeRepository commandeRepository, ProduitRepository produitRepository, PersonneRepository personneRepository, KeycloakService keycloakService) {
        this.commandeRepository = commandeRepository;
        this.produitRepository = produitRepository;
        this.personneRepository = personneRepository;
        this.keycloakService = keycloakService;
    }


    @Override
    public List<Commande> getAllCommandes(String keycloakUserId) {
        try {
            List<String> userRoles = keycloakService.getUserRoles(keycloakUserId);

            if (userRoles.contains("client_admin")) {
                return (List<Commande>) commandeRepository.findAll();
            } else {
                return commandeRepository.findByPersonneKeycloakId(keycloakUserId);
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la récupération des commandes : " + e.getMessage());
        }
    }

    @Override
    public Commande ajouterCommande(Commande commande) {
        try {

            Personne personne = personneRepository.findByTel(commande.getPersonne().getTel())
                    .orElseThrow(() -> new IllegalArgumentException("Personne non trouvée avec le numéro de téléphone : " + commande.getPersonne().getTel()));

            if (!personne.getNom().equalsIgnoreCase(commande.getPersonne().getNom())) {
                throw new IllegalArgumentException("Le nom fourni ne correspond pas à celui enregistré pour ce numéro de téléphone.");
            }

            Produit produit = produitRepository.findByLibeller(commande.getProduit().getLibeller())
                    .orElseThrow(() -> new NoSuchElementException("Produit non trouvé avec le libeller : " + commande.getProduit().getLibeller()));

            if ( !(commande.getQuantite() > 0) ) {
                throw new IllegalArgumentException("a quantité demandée est negative");
            }

            if (commande.getQuantite() > produit.getQuantite()) {
                throw new IllegalArgumentException("La quantité demandée dépasse le stock disponible");
            }



            Long montant = produit.getPrix() * commande.getQuantite();
            produit.setQuantite(produit.getQuantite() - commande.getQuantite());


            StatutCommande statutCommande = StatutCommande.valueOf(commande.getStatut().name());

            Commande nouvellecCmmande = new Commande();
            nouvellecCmmande.setProduit(produit);
            nouvellecCmmande.setStatut(statutCommande);
            nouvellecCmmande.setQuantite(commande.getQuantite());
            nouvellecCmmande.setMontant(montant);
            nouvellecCmmande.setPersonne(personne);


            return commandeRepository.save(nouvellecCmmande);

        } catch (IllegalArgumentException | NoSuchElementException e) {
            throw new RuntimeException("Erreur lors de l'ajout de la commande : " + e.getMessage());
        }
    }


    @Override
    public Commande modifierCommande(Long commandeId, Commande commande) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String keycloakUserId = authentication.getName();

            List<String> userRoles = keycloakService.getUserRoles(keycloakUserId);


            Commande existingCommande  = commandeRepository.findById(commandeId)
                        .orElseThrow(() -> new IllegalArgumentException("Commande non trouvée avec l'ID : " + commandeId));


                Personne personne = personneRepository.findByTel(commande.getPersonne().getTel())
                        .orElseThrow(() -> new IllegalArgumentException("Personne non trouvée avec le numéro de téléphone : " + commande.getPersonne().getTel()));

                if (!personne.getNom().equalsIgnoreCase(commande.getPersonne().getNom())) {
                    throw new IllegalArgumentException("Le nom fourni ne correspond pas à celui enregistré pour ce numéro de téléphone.");
                }

                Produit produit = produitRepository.findByLibeller(commande.getProduit().getLibeller())
                        .orElseThrow(() -> new NoSuchElementException("Produit non trouvé avec le libeller : " + commande.getProduit().getLibeller()));

            if (commande.getQuantite() <= 0) {
                throw new IllegalArgumentException("La quantité demandée est negative");
            }

                if (commande.getQuantite() > produit.getQuantite()) {
                    throw new IllegalArgumentException("La quantité demandée dépasse le stock disponible");
                }
            if (userRoles.contains("client_admin")) {
                //Long montant = produit.getPrix() * commande.getQuantite();
                //produit.setQuantite(produit.getQuantite() - commande.getQuantite());

                Long montant = produit.getPrix() * commande.getQuantite();
                Long quantite = commande.getQuantite() -existingCommande.getQuantite();
                produit.setQuantite(produit.getQuantite() - quantite);


                StatutCommande statutCommande = StatutCommande.valueOf(commande.getStatut().name());

                existingCommande.setProduit(produit);
                existingCommande.setStatut(statutCommande);
                existingCommande.setQuantite(commande.getQuantite());
                existingCommande.setMontant(montant);
                existingCommande.setPersonne(personne);

            }else{
                StatutCommande statutCommande = StatutCommande.valueOf(commande.getStatut().name());
                existingCommande.setStatut(statutCommande);
            }
            return commandeRepository.save(existingCommande);

        } catch (IllegalArgumentException | NoSuchElementException e) {
            throw new RuntimeException("Erreur lors de la modification de la commande : " + e.getMessage());
        }
    }

    @Override
    public void supprimerCommande(Long commandeId) {
        try {
            Commande commande = commandeRepository.findById(commandeId)
                    .orElseThrow(() -> new IllegalArgumentException("Commande non trouvée avec l'ID : " + commandeId));
            Produit produit = produitRepository.findByLibeller(commande.getProduit().getLibeller())
                            .orElseThrow(()-> new NoSuchElementException("Produit non trouvé avec le libeller :  "+ commande.getProduit().getLibeller()));
            produit.setQuantite(produit.getQuantite() + commande.getQuantite());

            commandeRepository.delete(commande);

        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Erreur lors de la suppression de la commande : " + e.getMessage());
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

    @Override
    public boolean saveFile(MultipartFile fileToSave) {
        try {
            if (fileToSave == null) {
                throw new IllegalArgumentException("Fichier à sauvegarder est null");
            }

            File targetFile = new File(STORAGE_DIRECTORY + '\\' + fileToSave.getOriginalFilename());

            if (!Objects.equals(targetFile.getParent(), STORAGE_DIRECTORY)) {
                throw new SecurityException("Nom de fichier non pris en charge");
            }

            Files.copy(fileToSave.getInputStream(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return true;

        } catch (IOException | IllegalArgumentException | SecurityException e) {
            log.info("Erreur lors de l'upload du fichier", e);
            return false;
        }
    }
}