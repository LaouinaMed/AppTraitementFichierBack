package com.example.demoapp.Iservices;

import com.example.demoapp.entities.LogErreur;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface LogErreurService {

    Page<LogErreur> getLogs(String filter, Pageable pageable);
}
