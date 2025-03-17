package com.example.demoapp.services;

import com.example.demoapp.Iservices.PersonneService;
import com.example.demoapp.entities.Personne;
import com.example.demoapp.repositories.PersonneRepository;
import jakarta.ws.rs.core.Response;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class PersonneServiceImpl implements PersonneService {

    private final PersonneRepository personneRepository;
    private KeycloakService keycloakService;
    private static final Logger log = Logger.getLogger(PersonneServiceImpl.class.getName());

    @Autowired
    public PersonneServiceImpl(PersonneRepository personneRepository,KeycloakService keycloakService) {
        this.personneRepository = personneRepository;
        this.keycloakService = keycloakService;
    }

    private boolean isCinValid(String cin) {
        return cin != null && cin.matches("[A-Z][A-Z0-9]\\d{5}");
    }

    private boolean isTelValid(String tel) {
        return tel != null && tel.matches("^(212[6-7]\\d{8}|0[67]\\d{8})$");
    }

    private static final String STORAGE_DIRECTORY = "C:\\Users\\simed\\Desktop\\ReaderBatch";



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
            log.log(Level.SEVERE, "Erreur lors de l'upload du fichier", e);
            return false;
        }
    }


    @Override
    public Personne addPersonne(Personne personne) {
        try {
            if (!isCinValid(personne.getCin())) {
                throw new IllegalArgumentException("CIN invalide");
            }
            if (!isTelValid(personne.getTel())) {
                throw new IllegalArgumentException("Numéro de téléphone invalide");
            }

            if (personneRepository.findByCin(personne.getCin()).isPresent()) {
                throw new IllegalArgumentException("Le CIN existe déjà");
            }
            if (personneRepository.findByTel(personne.getTel()).isPresent()) {
                throw new IllegalArgumentException("Le numéro de téléphone existe déjà");
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

            if (personneDetails.getCin() != null && !personneDetails.getCin().equals(personne.getCin())
                    && !isCinValid(personneDetails.getCin())) {
                throw new IllegalArgumentException("CIN invalide");
            }

            if (personneDetails.getTel() != null && !personneDetails.getTel().equals(personne.getTel())
                    && !isTelValid(personneDetails.getTel())) {
                throw new IllegalArgumentException("Numéro de téléphone invalide");
            }

            personne.setCin(personneDetails.getCin());
            personne.setNom(personneDetails.getNom());
            personne.setPrenom(personneDetails.getPrenom());
            personne.setTel(personneDetails.getTel());
            personne.setAdresse(personneDetails.getAdresse());

            return personneRepository.save(personne);
        } catch (Exception e) {
            log.log(Level.SEVERE, "Erreur lors de la mise à jour de la personne", e);
            throw new RuntimeException("Erreur lors de la mise à jour de la personne : " + e.getMessage(), e);
        }
    }

    @Override
    public void deletePersonne(Long id) {
        try {
            Personne personne = personneRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Personne non trouvée"));
            personneRepository.delete(personne);
        } catch (Exception e) {
            log.log(Level.SEVERE, "Erreur lors de la suppression de la personne", e);
            throw new RuntimeException("Erreur lors de la suppression de la personne : " + e.getMessage(), e);
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

    @Override
    public Iterable<Personne> getAllPersonnes() {
        try {
            return personneRepository.findAll();
        } catch (Exception e) {
            log.log(Level.SEVERE, "Erreur lors de la récupération des personnes", e);
            throw new RuntimeException("Erreur lors de la récupération des personnes", e);
        }
    }
}
