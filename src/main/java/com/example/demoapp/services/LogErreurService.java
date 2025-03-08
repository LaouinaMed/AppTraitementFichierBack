package com.example.demoapp.services;

import com.example.demoapp.entities.LogErreur;
import com.example.demoapp.repositories.LogErreurRepository;
import org.springframework.stereotype.Service;

@Service
public class LogErreurService {

    private final LogErreurRepository logErreurRepository;

    public LogErreurService(LogErreurRepository logErreurRepository){
        this.logErreurRepository = logErreurRepository;
    }

    public Iterable<LogErreur> getAllLogs() {
        return logErreurRepository.findAll();
    }

}
