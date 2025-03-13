package com.example.demoapp.service;

import com.example.demoapp.entities.LogErreur;
import com.example.demoapp.repositories.LogErreurRepository;
import com.example.demoapp.services.LogErreurServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogErreurServiceTest {

    @Mock
    private LogErreurRepository logErreurRepository;

    @InjectMocks
    private LogErreurServiceImpl logErreurService;

    private LogErreur log1, log2;

    @BeforeEach
    void setUp() {
        log1 = new LogErreur(1L, "file1.txt", 10, "Erreur 1", "2024-03-04");
        log2 = new LogErreur(2L, "file2.txt", 20, "Erreur 2", "2024-03-05");
    }

    @Test
    void testGetAllLogs_Success() {
        List<LogErreur> logs = Arrays.asList(log1, log2);
        when(logErreurRepository.findAll()).thenReturn(logs);

        Iterable<LogErreur> result = logErreurService.getAllLogs();

        assertNotNull(result);
        assertEquals(2, ((List<LogErreur>) result).size());
        verify(logErreurRepository, times(1)).findAll();
    }
}
