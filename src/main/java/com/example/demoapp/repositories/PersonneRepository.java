package com.example.demoapp.repositories;

import com.example.demoapp.entities.Personne;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PersonneRepository extends JpaRepository<Personne, Long> {

    Optional<Personne> findByCin(String cin);

    Optional<Personne> findByTel(String tel);


}
