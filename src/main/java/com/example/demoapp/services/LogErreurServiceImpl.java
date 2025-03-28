package com.example.demoapp.services;

import com.example.demoapp.Iservices.LogErreurService;
import com.example.demoapp.entities.LogErreur;
import com.example.demoapp.repositories.LogErreurRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LogErreurServiceImpl implements LogErreurService {

    private final LogErreurRepository logErreurRepository;

    public LogErreurServiceImpl(LogErreurRepository logErreurRepository){
        this.logErreurRepository = logErreurRepository;
    }


    @Override
    public Page<LogErreur> getLogs(String filter, Pageable pageable) {
        try {
            List<LogErreur> allLogs;

            if (filter != null && !filter.isEmpty()) {
                return logErreurRepository.findByMessageContainingIgnoreCase(filter, pageable);
            } else {
                return logErreurRepository.findAll(pageable);
            }

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors du filtrage des logs", e);
        }
    }
}
