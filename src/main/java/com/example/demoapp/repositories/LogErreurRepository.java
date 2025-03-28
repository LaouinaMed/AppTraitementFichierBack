package com.example.demoapp.repositories;

import com.example.demoapp.entities.LogErreur;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LogErreurRepository  extends JpaRepository<LogErreur, Long> {

    Page<LogErreur> findByMessageContainingIgnoreCase(String filter, Pageable pageable);


}
