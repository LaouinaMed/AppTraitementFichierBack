package com.example.demoapp.services;

import com.example.demoapp.Iservices.PersonneService;
import com.example.demoapp.entities.Commande;
import com.example.demoapp.entities.Personne;
import com.example.demoapp.entities.Produit;
import com.example.demoapp.repositories.CommandeRepository;
import com.example.demoapp.repositories.PersonneRepository;
import com.example.demoapp.repositories.ProduitRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class PersonneServiceImpl implements PersonneService {

    private final PersonneRepository personneRepository;
    private final ProduitRepository produitRepository;

    private KeycloakServiceImpl keycloakService;
    private CommandeRepository commandeRepository;
    private static final Logger log = Logger.getLogger(PersonneServiceImpl.class.getName());

    @Autowired
    public PersonneServiceImpl(PersonneRepository personneRepository, ProduitRepository produitRepository, KeycloakServiceImpl keycloakService, CommandeRepository commandeRepository) {
        this.personneRepository = personneRepository;
        this.produitRepository = produitRepository;
        this.keycloakService = keycloakService;
        this.commandeRepository = commandeRepository;
    }

    private boolean isCinValid(String cin) {
        return cin != null && cin.matches("[A-Z][A-Z0-9]\\d{5}");
    }

    private boolean isTelValid(String tel) {
        return tel != null && tel.matches("^(212[6-7]\\d{8}|0[67]\\d{8})$");
    }

    private boolean isEmailValid(String email){
        return email != null && email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    }

    private static final String STORAGE_DIRECTORY = "C:\\Users\\simed\\Desktop\\ReaderBatch";


    @Override
    public Personne addPersonne(Personne personne) {
        try {

            if (!isCinValid(personne.getCin())) {
                throw new IllegalArgumentException("CIN invalide");
            }
            if (!isTelValid(personne.getTel())) {
                throw new IllegalArgumentException("Numéro de téléphone invalide");
            }

            if (!isEmailValid(personne.getEmail())) {
                throw new IllegalArgumentException("Email invalide");
            }

/*
            if (personneRepository.findByCin(personne.getCin()).isPresent()) {
                throw new IllegalArgumentException("Le CIN existe déjà");
            }
            if (personneRepository.findByTel(personne.getTel()).isPresent()) {
                throw new IllegalArgumentException("Le numéro de téléphone existe déjà");
            }
*/
            if(personneRepository.findByCinOrTelOrEmail(
                    personne.getCin(),
                    personne.getTel(),
                    personne.getEmail()).isPresent()
            ){
                throw new IllegalArgumentException("Le CIN, numéro de téléphone ou email existe déjà");

            }
            String keycloakUserId = keycloakService.createUserInKeycloak(personne);
            personne.setKeycloakId(keycloakUserId);

            return personneRepository.save(personne);

        } catch (Exception e) {
            log.log(Level.SEVERE, "Erreur lors de l'ajout de la personne", e);
            throw new RuntimeException("Erreur lors de l'ajout de la personne : " + e.getMessage(), e);
        }
    }




    @Override
    public Personne updatePersonne(Long id, Personne personneDetails) {
        try {
            Personne personne = personneRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Personne non trouvée"));


            if ( personneRepository.existsByCinAndIdNot(personneDetails.getCin(),id)
                || personneRepository.existsByTelAndIdNot(personneDetails.getTel(),id)
                || personneRepository.existsByEmailAndIdNot(personneDetails.getEmail(),id)) {

                throw new IllegalArgumentException("CIN ou Tel ou Email existe deja");
            }

            if (personneDetails.getCin() != null && !isCinValid(personneDetails.getCin())) {
                throw new IllegalArgumentException("CIN invalide");
            }

            if (personneDetails.getTel() != null && !isTelValid(personneDetails.getTel())) {
                throw new IllegalArgumentException("Numéro de téléphone invalide");
            }

            if(personneDetails.getEmail() != null && !isEmailValid(personneDetails.getEmail())){
                throw new IllegalArgumentException("Numéro de téléphone invalide");
            }


            personne.setCin(personneDetails.getCin());
            personne.setNom(personneDetails.getNom());
            personne.setPrenom(personneDetails.getPrenom());
            personne.setTel(personneDetails.getTel());
            personne.setAdresse(personneDetails.getAdresse());
            personne.setEmail(personneDetails.getEmail());

            keycloakService.updateUserInKeycloak(personne.getKeycloakId(), personne);


            return personneRepository.save(personne);
        } catch (Exception e) {
            log.log(Level.SEVERE, "Erreur lors de la mise à jour de la personne", e);
            throw new RuntimeException("Erreur lors de la mise à jour de la personne : " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void deletePersonne(Long id) {
        try {
            Personne personne = personneRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Personne non trouvée"));

            List<Commande> commandes = commandeRepository.findByPersonne(personne);

            if (!(commandes == null || commandes.isEmpty())) {
                for(Commande commande : commandes){
                    Produit produit = commande.getProduit();

                    produit.setQuantite(produit.getQuantite()+ commande.getQuantite());

                    produitRepository.save(produit);
                    commandeRepository.delete(commande);

                }
            }
            keycloakService.deleteUserInKeycloak(personne.getKeycloakId());
            personneRepository.delete(personne);

        } catch (Exception e) {
            log.log(Level.SEVERE, "Erreur lors de la suppression de la personne", e);
            throw new RuntimeException("Erreur lors de la suppression de la personne : " + e.getMessage(), e);
        }
    }


    @Override
    public List<Personne> getAllPersonnes() {
        try {
            return personneRepository.findAll();
        } catch (Exception e) {
            log.log(Level.SEVERE, "Erreur lors de la récupération des personnes", e);
            throw new RuntimeException("Erreur lors de la récupération des personnes", e);
        }
    }

    @Override
    public Optional<Personne> getPersonneById(Long id) {
        try {
            return personneRepository.findById(id);
        } catch (Exception e) {
            log.log(Level.SEVERE, "Erreur lors de la récupération de la personne", e);
            throw new RuntimeException("Erreur lors de la récupération de la personne", e);
        }
    }
}
