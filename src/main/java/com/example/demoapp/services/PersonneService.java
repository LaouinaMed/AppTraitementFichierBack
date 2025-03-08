package com.example.demoapp.services;

import com.example.demoapp.entities.Personne;
import com.example.demoapp.repositories.PersonneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.Optional;

@Service
public class PersonneService {

    private final PersonneRepository personneRepository;


    @Autowired
    public PersonneService(PersonneRepository personneRepository) {
        this.personneRepository = personneRepository;
    }

    private boolean isCinValid(String cin) {
        return cin != null && cin.matches("[A-Z][A-Z0-9]\\d{5}");
    }
    private boolean isTelValid(String tel) {
        return tel != null && tel.matches("^(212[6-7]\\d{8}|0[67]\\d{8})$");
    }

    private static final String STORAGR_DIRECTORY = "C:\\Users\\simed\\Desktop\\ReaderBatch";
    public void saveFile(MultipartFile fileToSave) throws IOException {
        if(fileToSave == null){
            throw new NullPointerException("Fichier a sauvgarder est null");
        }
        var targetFile = new File(STORAGR_DIRECTORY + '\\' +fileToSave.getOriginalFilename());

        if(!Objects.equals(targetFile.getParent(),STORAGR_DIRECTORY)){
            throw new SecurityException("Unsupported fileName");
        }

        Files.copy(fileToSave.getInputStream(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }
    // Méthode pour ajouter une personne
    public Personne addPersonne(Personne personne) {
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

        return personneRepository.save(personne);
    }

    public Personne updatePersonne(Long id, Personne personneDetails) {
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
    }

    public void deletePersonne(Long id) {
        Personne personne = personneRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Personne non trouvée"));
        personneRepository.delete(personne);
    }

    public Optional<Personne> getPersonneById(Long id) {
        return personneRepository.findById(id);
    }

    public Iterable<Personne> getAllPersonnes() {
        return personneRepository.findAll();
    }
}
