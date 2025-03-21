package com.example.demoapp.services;

import com.example.demoapp.Iservices.ProduitService;
import com.example.demoapp.entities.Produit;
import com.example.demoapp.repositories.ProduitRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProduitServiceImpl implements ProduitService {

    private final ProduitRepository produitRepository;


    public ProduitServiceImpl(ProduitRepository produitRepository) {
        this.produitRepository = produitRepository;
    }


    @Override
    public Produit addProduit(Produit produit) {
       try{
           if(produitRepository.findByLibeller(produit.getLibeller()).isPresent()){
               throw new IllegalArgumentException("Le produit existe Deja");
           }
           else if(produit.getQuantite() <= 0){
               throw new IllegalArgumentException("Qte doit etre superieure de 0");

           }

           else if(produit.getPrix() <= 0){
               throw new IllegalArgumentException("Prix doit etre superieure de 0");

           }

           else {
               return produitRepository.save(produit);
           }
       }catch (Exception e){
           throw new RuntimeException("Erreur lors de l'ajout de la personne : " + e.getMessage(), e);
       }
    }

    @Override
    public Produit updateProduit(Long id, Produit produitDetails) {
    try{

        Optional<Produit> produitOptional = produitRepository.findById(id);
        if (!produitOptional.isPresent()) {
            throw new IllegalArgumentException("Le produit avec l'ID " + id + " n'existe pas");
        }

        Produit produit = produitOptional.get();

        if (produitRepository.findByLibeller(produitDetails.getLibeller())
                .filter(existingProduit -> !existingProduit.getId().equals(produit.getId()))
                .isPresent()) {
            throw new IllegalArgumentException("Un produit avec ce libellé existe déjà");
        }

        if (produitDetails.getQuantite() <= 0) {
            throw new IllegalArgumentException("La quantité doit être supérieure à 0");
        }

        produit.setLibeller(produitDetails.getLibeller());
        produit.setQuantite(produitDetails.getQuantite());
        produit.setPrix(produitDetails.getPrix());

        return produitRepository.save(produit);

    } catch (Exception e) {
        throw new RuntimeException("Erreur lors de la mise à jour du produit : " + e.getMessage(), e);
    }
}

    @Override
    public void deleteProduit(Long id) {
        try{
            Produit produit =produitRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Produit non trouvée"));
            produitRepository.delete(produit);
        }catch (Exception e){
            throw new RuntimeException("Erreur lors de la suppression de la produit : " + e.getMessage(), e);

        }

    }

    @Override
    public Optional<Produit> getProduitById(Long id) {
        return Optional.empty();
    }

    @Override
    public List<Produit> getAllProduits() {
        try {
            return produitRepository.findAll();
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la récupération des personnes", e);
        }
    }
}
