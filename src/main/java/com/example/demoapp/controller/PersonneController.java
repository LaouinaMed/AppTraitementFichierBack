package com.example.demoapp.controller;

import com.example.demoapp.entities.Personne;
import com.example.demoapp.services.PersonneService;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/personnes")
@RequiredArgsConstructor

public class PersonneController {


    private final PersonneService personneService;
    private static final Logger log = Logger.getLogger(PersonneController.class.getName());

    @PostMapping("/upload")
    @PreAuthorize("hasRole('client_admin')")

    public boolean uploadFile(@RequestParam("file")MultipartFile file){

        try{
            personneService.saveFile(file);
            return true;
        }catch (IOException e){
            log.log(Level.SEVERE,"Exception upload",e );
        }
        return false;
    }

    @PostMapping
    @PreAuthorize("hasRole('client_admin')")

    public ResponseEntity<Personne> addPersonne(@RequestBody Personne personne) {
        try {
            if (personne == null) {
                throw new IllegalArgumentException("Personne cannot be null");
            }
            Personne savedPersonne = personneService.addPersonne(personne);
            return new ResponseEntity<>(savedPersonne, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>( HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('client_admin')")

    public ResponseEntity<Personne> updatePersonne(@PathVariable Long id,@Nullable @RequestBody Personne personne) {


        try {

            Personne updatedPersonne = personneService.updatePersonne(id, personne);
            return new ResponseEntity<>(updatedPersonne, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('client_admin')")

    public ResponseEntity<Void> deletePersonne(@PathVariable Long id) {
        try {
            personneService.deletePersonne(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('client_admin')")

    public ResponseEntity<List<Personne>> getAllPersonnes() {

        List<Personne> personnes = (List<Personne>) personneService.getAllPersonnes();
        return new ResponseEntity<>(personnes, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('client_admin')")

    public ResponseEntity<Personne> getPersonneById(@PathVariable Long id) {
        Optional<Personne> personne = personneService.getPersonneById(id);
        if (personne.isPresent()) {
            return new ResponseEntity<>(personne.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

    }
}
