package com.example.demoapp.repositories;

import com.example.demoapp.entities.Personne;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PersonneRepository extends JpaRepository<Personne, Long> {

    Optional<Personne> findByCin(String cin);

    Optional<Personne> findByTel(String tel);

    boolean existsByCinAndIdNot(String cin, Long id);
    boolean existsByTelAndIdNot(String tel, Long id);
    boolean existsByEmailAndIdNot(String email, Long id);



    @Query("SELECT p FROM Personne p where p.cin =:cin OR p.tel = :tel OR p.email= :email")
    Optional<Personne> findByCinOrTelOrEmail(@Param("cin") String cin,@Param("tel") String tel, @Param("email") String email);


}
