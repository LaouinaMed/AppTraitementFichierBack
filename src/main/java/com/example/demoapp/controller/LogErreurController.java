package com.example.demoapp.controller;

import com.example.demoapp.entities.LogErreur;
import com.example.demoapp.services.LogErreurServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/personnes")
@RequiredArgsConstructor
public class LogErreurController {
//cont
    //sd
    private final LogErreurServiceImpl logErreurService;
    @GetMapping("/logs")
    @PreAuthorize("hasRole('client_admin')")

    public ResponseEntity<List<LogErreur>> getAllLogs() {

        List<LogErreur> logs = (List<LogErreur>) logErreurService.getAllLogs();
        return new ResponseEntity<>(logs, HttpStatus.OK);
    }
}
