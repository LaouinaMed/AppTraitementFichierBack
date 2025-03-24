package com.example.demoapp.batch.processor;

import com.example.demoapp.dto.DtoCommande;
import com.example.demoapp.entities.Commande;
import com.example.demoapp.entities.LogErreur;
import com.example.demoapp.entities.Personne;
import com.example.demoapp.entities.Produit;
import com.example.demoapp.enumeration.StatutCommande;
import com.example.demoapp.repositories.CommandeRepository;
import com.example.demoapp.repositories.LogErreurRepository;
import com.example.demoapp.repositories.PersonneRepository;
import com.example.demoapp.repositories.ProduitRepository;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ItemProcessor;

import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Pattern;


public class CommandeProcessor implements ItemProcessor<DtoCommande, Commande> {

    private static final Logger logger = Logger.getLogger(CommandeProcessor.class.getName());
    private CommandeRepository commandeRepository;
    private  PersonneRepository personneRepository;
    private ProduitRepository produitRepository;

    private LogErreurRepository logErreurRepository;
    private Set<String> processedCins = new HashSet<>();
    private Set<String> processedTels = new HashSet<>();
    private List<String> erreurs = new ArrayList<>();
    private int ligneActuelle = 1;
    private String fileName ="Fichier inconnu";
    private String date;



    //@Autowired
    public CommandeProcessor(CommandeRepository commandeRepository, LogErreurRepository logErreurRepository, PersonneRepository personneRepository, ProduitRepository produitRepository){
        this.commandeRepository = commandeRepository;
        this.logErreurRepository = logErreurRepository;
        this.personneRepository = personneRepository;
        this.produitRepository = produitRepository;
    }

    @BeforeStep
    public void beforeStep(StepExecution stepExecution) {
        this.fileName = stepExecution.getJobParameters().getString("fileName", "Fichier inconnu");
        this.date = stepExecution.getJobParameters().getString("formattedDate","Date Inconu");
    }


    private boolean isAdressValid(String adress) {
        return adress != null && Pattern.matches("^[A-Za-z0-9,.'\\-\\s]{5,}$", adress);
    }


    public void afficherErreurs() {
        if (!erreurs.isEmpty()) {
            logger.info("*********************************** Erreurs détectées pendant l'importation : ****************************************");
            for (String erreur : erreurs) {
                logger.info(erreur);
            }

        } else {
            logger.info("*********************************** Aucune erreur détectée.****************************************");
        }
    }


    @Override
    public Commande process(DtoCommande dtoCommande) throws Exception {
        ligneActuelle++;
        boolean isValid = true;
        String erreurMsg = "❌ ERREUR - [";
        String fichier = "] - Fichier: ";
        String ligne = " | Ligne ";

        StatutCommande statut = dtoCommande.getStatut();

        if (dtoCommande.getNom() == null || dtoCommande.getNom().isEmpty()
                || dtoCommande.getTel() == null || dtoCommande.getTel().isEmpty()
                || dtoCommande.getLibeller() == null || dtoCommande.getLibeller().isEmpty()
                || dtoCommande.getQuantite() == null
                || dtoCommande.getStatut() == null) {

            String message = erreurMsg + date + fichier + fileName + ligne + ligneActuelle + ") : Ligne incomplète ou vide";
            logErreurRepository.save(new LogErreur(null, fileName, ligneActuelle, message, date));
            return null;
        }
        Optional<Personne> personneOptional = personneRepository.findByTel(dtoCommande.getTel());
        Optional<Produit> produitOptional = produitRepository.findByLibeller(dtoCommande.getLibeller());

        if (!personneOptional.isPresent()) {
            String message = erreurMsg + date + fichier + fileName + ligne + ligneActuelle + " : Personne non trouvée avec le numéro de téléphone " + dtoCommande.getTel();
            logErreurRepository.save(new LogErreur(null, fileName, ligneActuelle, message, date));
            isValid = false;
        }

        if (!produitOptional.isPresent()) {
            String message = erreurMsg + date + fichier + fileName + ligne + ligneActuelle + " : Produit non trouvé avec le libeller : " + dtoCommande.getLibeller();
            logErreurRepository.save(new LogErreur(null, fileName, ligneActuelle, message, date));
            isValid = false;
        }

        if (dtoCommande.getQuantite() == null || dtoCommande.getQuantite() > produitOptional.get().getQuantite()) {
            String message = erreurMsg + date + fichier + fileName + ligne + ligneActuelle + " : La quantité demandée dépasse le stock disponible";
            logErreurRepository.save(new LogErreur(null, fileName, ligneActuelle, message, date));
            isValid = false;
        }

        if (statut == null && (statut.name().equals("CONFIRMEE") || statut.name().equals("REJETEE"))) {
            String message = erreurMsg + date + fichier + fileName + ligne + ligneActuelle + " : Verifier le statut";
            logErreurRepository.save(new LogErreur(null, fileName, ligneActuelle, message, date));
            isValid = false;
        }

        if (isValid) {
            Commande commande = new Commande();
            Long montant = produitOptional.get().getPrix() * dtoCommande.getQuantite();
            commande.setQuantite(dtoCommande.getQuantite());
            commande.setProduit(produitOptional.get());
            commande.setMontant(montant);
            commande.setPersonne(personneOptional.get());
            commande.setStatut(statut);
            return commande;
        } else {
            return null;
        }
    }

}