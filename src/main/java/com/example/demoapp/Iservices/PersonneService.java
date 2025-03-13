package com.example.demoapp.Iservices;

import com.example.demoapp.entities.Personne;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

public interface PersonneService {
    boolean saveFile(MultipartFile fileToSave) throws IOException;
    Personne addPersonne(Personne personne);
    Personne updatePersonne(Long id, Personne personneDetails);
    void deletePersonne(Long id);
    Optional<Personne> getPersonneById(Long id);
    Iterable<Personne> getAllPersonnes();
}
