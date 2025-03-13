package com.example.demoapp.services;

import com.example.demoapp.Iservices.LogErreurService;
import com.example.demoapp.entities.LogErreur;
import com.example.demoapp.repositories.LogErreurRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LogErreurServiceImpl implements LogErreurService {

    private final LogErreurRepository logErreurRepository;

    public LogErreurServiceImpl(LogErreurRepository logErreurRepository){
        this.logErreurRepository = logErreurRepository;
    }

    @Override
    public List<LogErreur> getAllLogs() {
        try {
            return logErreurRepository.findAll();
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la récupération des logs", e);
        }
    }


}
