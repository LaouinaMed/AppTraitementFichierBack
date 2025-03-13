package com.example.demoapp.controller;

import com.example.demoapp.entities.Personne;
import com.example.demoapp.services.PersonneServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PersonneControllerTest {

    @Mock
    private PersonneServiceImpl personneService;

    @InjectMocks
    private PersonneController personneController;

    private Personne personne;

    @BeforeEach
    void setUp() {
        personne = new Personne(1L, "A123456", "John", "Doe", "0612345678", "Rue de Paris");
    }

    @Test
    void testAddPersonne_Success() {
        when(personneService.addPersonne(any(Personne.class))).thenReturn(personne);

        ResponseEntity<Personne> response = personneController.addPersonne(personne);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testAddPersonne_Fail_NullBody() {
        ResponseEntity<Personne> response = personneController.addPersonne(null);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testUpdatePersonne_Success() {
        when(personneService.updatePersonne(eq(1L), any(Personne.class))).thenReturn(personne);

        ResponseEntity<Personne> response = personneController.updatePersonne(1L, personne);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testUpdatePersonne_Fail_BadRequest() {
        when(personneService.updatePersonne(anyLong(), any())).thenThrow(new IllegalArgumentException());

        ResponseEntity<Personne> response = personneController.updatePersonne(1L, personne);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testDeletePersonne_Success() {
        doNothing().when(personneService).deletePersonne(1L);

        ResponseEntity<Void> response = personneController.deletePersonne(1L);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void testGetPersonneById_Success() {
        when(personneService.getPersonneById(1L)).thenReturn(Optional.of(personne));

        ResponseEntity<Personne> response = personneController.getPersonneById(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("John", response.getBody().getNom());
    }

    @Test
    void testGetPersonneById_Fail_NotFound() {
        when(personneService.getPersonneById(1L)).thenReturn(Optional.empty());

        ResponseEntity<Personne> response = personneController.getPersonneById(1L);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
