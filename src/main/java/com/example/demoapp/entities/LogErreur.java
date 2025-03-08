package com.example.demoapp.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "logs_erreurs")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class LogErreur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;
    private int ligne;
    private String message;

    @Column(name = "date_creation")
    private String dateCreation;
}
