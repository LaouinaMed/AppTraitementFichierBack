package com.example.demoapp.Iservices;

import com.example.demoapp.entities.Commande;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface CommandeService {

    public Commande ajouterCommande(Commande commande) ;

    public Commande modifierCommande(Long commandeId, Commande commande) ;

    public List<String> getStatutsDisponibles() ;
    public void supprimerCommande(Long commandeId) ;

    List<Commande> getAllCommandes(String keycloakUserId);

    boolean saveFile(MultipartFile fileToSave) throws IOException;
    }

