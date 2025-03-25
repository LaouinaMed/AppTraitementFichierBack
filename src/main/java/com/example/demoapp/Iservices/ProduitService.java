package com.example.demoapp.Iservices;

import com.example.demoapp.entities.Produit;

import java.util.List;
import java.util.Optional;

public interface ProduitService {

    Produit addProduit(Produit produit);

    Produit updateProduit(Long id , Produit produitDetails);

    void deleteProduit(Long id);
    List<Produit> getAllProduits();

}
