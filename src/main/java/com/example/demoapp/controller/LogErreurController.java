package com.example.demoapp.controller;

import com.example.demoapp.entities.LogErreur;
import com.example.demoapp.services.LogErreurServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/personnes")
@RequiredArgsConstructor
public class LogErreurController {

    private final LogErreurServiceImpl logErreurService;

    @GetMapping("/logs")
    @PreAuthorize("hasRole('client_admin')")
    public ResponseEntity<Page<LogErreur>> getAllLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String filter) {

        Pageable pageable = PageRequest.of(page, size);

        Page<LogErreur> logs = logErreurService.getLogs(filter, pageable);

        return ResponseEntity.status(HttpStatus.OK).body(logs);
    }
}