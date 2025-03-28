package com.example.demoapp.Iservices;

import com.example.demoapp.entities.Personne;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface PersonneService {
   // boolean saveFile(MultipartFile fileToSave) throws IOException;
    Personne addPersonne(Personne personne);
    Personne updatePersonne(Long id, Personne personneDetails);
    void deletePersonne(Long id);
    List<Personne> getAllPersonnes();
    Optional<Personne> getPersonneById(Long id);
}
