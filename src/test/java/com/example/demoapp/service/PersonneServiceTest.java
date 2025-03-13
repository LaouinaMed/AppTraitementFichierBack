package com.example.demoapp.service;

import com.example.demoapp.entities.Personne;
import com.example.demoapp.repositories.PersonneRepository;
import com.example.demoapp.services.PersonneServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PersonneServiceTest {

    @Mock
    private PersonneRepository personneRepository;

    @InjectMocks
    private PersonneServiceImpl personneService;

    private Personne personne;

    @BeforeEach
    void setUp() {
        personne = new Personne(1L, "A123456", "John", "Doe", "0612345678", "Rue de Paris");
    }

    @Test
    void testAddPersonne_Success() {
        when(personneRepository.findByCin(personne.getCin())).thenReturn(Optional.empty());
        when(personneRepository.findByTel(personne.getTel())).thenReturn(Optional.empty());
        when(personneRepository.save(personne)).thenReturn(personne);

        Personne savedPersonne = personneService.addPersonne(personne);
        assertNotNull(savedPersonne);
        assertEquals(personne.getCin(), savedPersonne.getCin());
    }

    @Test
    void testAddPersonne_Fail_CinExists() {
        when(personneRepository.findByCin(personne.getCin())).thenReturn(Optional.of(personne));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> personneService.addPersonne(personne));
        assertEquals("Le CIN existe déjà", exception.getMessage());
    }

    @Test
    void testUpdatePersonne_Success() {
        Personne updatedDetails = new Personne(null, "B987654", "Jane", "Doe", "0623456789", "Avenue Hassan");
        when(personneRepository.findById(1L)).thenReturn(Optional.of(personne));
        when(personneRepository.save(any(Personne.class))).thenReturn(updatedDetails);

        Personne updatedPersonne = personneService.updatePersonne(1L, updatedDetails);
        assertNotNull(updatedPersonne);
        assertEquals("Jane", updatedPersonne.getNom());
    }

    @Test
    void testUpdatePersonne_Fail_NotFound() {
        when(personneRepository.findById(1L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> personneService.updatePersonne(1L, personne));
        assertEquals("Personne non trouvée", exception.getMessage());
    }

    @Test
    void testDeletePersonne_Success() {
        when(personneRepository.findById(1L)).thenReturn(Optional.of(personne));

        assertDoesNotThrow(() -> personneService.deletePersonne(1L));
        verify(personneRepository, times(1)).delete(personne);
    }

    @Test
    void testDeletePersonne_Fail_NotFound() {
        when(personneRepository.findById(1L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> personneService.deletePersonne(1L));
        assertEquals("Personne non trouvée", exception.getMessage());
    }

    @Test
    void testGetPersonneById_Success() {
        when(personneRepository.findById(1L)).thenReturn(Optional.of(personne));

        Optional<Personne> foundPersonne = personneService.getPersonneById(1L);
        assertTrue(foundPersonne.isPresent());
        assertEquals("John", foundPersonne.get().getNom());
    }

    @Test
    void testSaveFile_Fail_NullFile() {
        Exception exception = assertThrows(NullPointerException.class, () -> personneService.saveFile(null));
        assertEquals("Fichier a sauvgarder est null", exception.getMessage());
    }
}
