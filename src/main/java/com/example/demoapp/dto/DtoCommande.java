package com.example.demoapp.dto;

import com.example.demoapp.enumeration.StatutCommande;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DtoCommande {


        private String nom;
        private String tel;
        private String libeller;
        private Long quantite;
        private String statut;
}
