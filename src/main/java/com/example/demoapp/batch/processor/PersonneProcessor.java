package com.example.demoapp.batch.processor;

import com.example.demoapp.entities.LogErreur;
import com.example.demoapp.entities.Personne;
import com.example.demoapp.repositories.CommandeRepository;
import com.example.demoapp.repositories.LogErreurRepository;
import com.example.demoapp.repositories.PersonneRepository;

import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ItemProcessor;

import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Pattern;


public class PersonneProcessor implements ItemProcessor<Personne, Personne> {

    private static final Logger logger = Logger.getLogger(PersonneProcessor.class.getName());
    private PersonneRepository personneRepository;
    private LogErreurRepository logErreurRepository;
    private Set<String> processedCins = new HashSet<>();
    private Set<String> processedTels = new HashSet<>();
    private List<String> erreurs = new ArrayList<>();
    private int ligneActuelle = 1;
    private String fileName ="Fichier inconnu";
    private String date;





    //@Autowired
    public PersonneProcessor(PersonneRepository personneRepository,LogErreurRepository logErreurRepository){
        this.personneRepository =personneRepository;
        this.logErreurRepository = logErreurRepository;
    }


    public void chargerCinsEtTels() {
        processedCins.clear();
        processedTels.clear();

        personneRepository.findAll().forEach(personne -> {
            processedCins.add(personne.getCin());
            processedTels.add(personne.getTel());
        });
    }

    @BeforeStep
    public void beforeStep(StepExecution stepExecution) {
        this.fileName = stepExecution.getJobParameters().getString("fileName", "Fichier inconnu");
        this.date = stepExecution.getJobParameters().getString("formattedDate","Date Inconu");
    }


    private boolean isCinValid(String cin) {

        return cin != null && Pattern.matches("[A-Z][A-Z0-9]\\d{5}", cin);
    }

    private boolean isTelValid(String tel) {

        return tel != null && tel.matches("^(212[6-7]\\d{8}|0[67]\\d{8})$"
        );
    }

    private boolean isNomValid(String nom) {
        return nom != null && Pattern.matches("[A-Za-z]{2,}", nom);
    }

    private boolean isPrenomValid(String prenom) {
        return prenom != null && Pattern.matches("[A-Za-z]{2,}", prenom);
    }

    private boolean isAdressValid(String prenom) {
        return prenom != null && Pattern.matches("^[A-Za-z0-9,.'\\-\\s]{5,}$", prenom);
    }


    @Override
    public Personne process(Personne personne) throws Exception {
        ligneActuelle++;
        boolean isValid = true;
        String erreurMsg ="❌ ERREUR - [";
        String fichier = "] - Fichier: ";
        String ligne = " | Ligne ";


        if (personne.getCin() == null || personne.getCin().isEmpty()
                || personne.getTel() == null || personne.getTel().isEmpty()
                || personne.getNom() == null || personne.getNom().isEmpty()
                || personne.getPrenom() == null || personne.getPrenom().isEmpty()
                || personne.getAdresse() == null || personne.getAdresse().isEmpty()) {
            String message = erreurMsg + date + fichier + fileName + ligne + ligneActuelle + ") : Ligne incomplète ou vide";
            logger.info(message);
            logErreurRepository.save(new LogErreur(null,fileName,ligneActuelle,message,date));
            return null;
        }


        if (processedCins.contains(personne.getCin()) || processedTels.contains(personne.getTel())) {
            String message = erreurMsg + date + fichier + fileName + ligne + ligneActuelle + ") : Doublon détecté pour CIN : " + personne.getCin() + " et Tel : " + personne.getTel();
            logger.info(message);
            logErreurRepository.save(new LogErreur(null,fileName,ligneActuelle,message,date));
            isValid = false;
        }


        if (!isCinValid(personne.getCin())) {
            String message = erreurMsg + date + fichier + fileName + ligne + ligneActuelle + ") : CIN invalide : " + personne.getCin();
            logger.info(message);
            logErreurRepository.save(new LogErreur(null,fileName,ligneActuelle,message,date));
            isValid = false;
        }

        if (!isTelValid(personne.getTel())) {
            String message = erreurMsg + date + fichier + fileName + ligne + ligneActuelle + ") : Numéro de téléphone invalide : " + personne.getTel();
            logger.info(message);
            logErreurRepository.save(new LogErreur(null,fileName,ligneActuelle,message,date));
            isValid = false;
        }

        if (!isPrenomValid(personne.getNom()) || !isNomValid(personne.getPrenom())) {
            String message = erreurMsg + date + fichier + fileName + ligne + ligneActuelle + ") : Nom invalide. Nom : " + personne.getNom() + " - Prénom : " + personne.getPrenom();
            logger.info(message);

            logErreurRepository.save(new LogErreur(null,fileName,ligneActuelle,message,date));
            isValid = false;
        }


        if (!isAdressValid(personne.getAdresse())) {
            String message = erreurMsg + date + fichier + fileName + ligne + ligneActuelle + ") :  Adresse invalide : " + personne.getAdresse();
            logger.info(message);

            logErreurRepository.save(new LogErreur(null,fileName,ligneActuelle,message,date));
            isValid = false;
        }


        if (personneRepository.findByCin(personne.getCin()).isPresent()) {
            String message = erreurMsg + date + fichier + fileName + ligne + ligneActuelle + ") : CIN existe déjà en base de données : " + personne.getCin();
            logger.info(message);

            logErreurRepository.save(new LogErreur(null,fileName,ligneActuelle,message,date));
            isValid = false;
        }

        if (personneRepository.findByTel(personne.getTel()).isPresent()) {
            String message = erreurMsg + date + fichier + fileName + ligne + ligneActuelle + ") : Numéro de téléphone existe déjà en base de données : " + personne.getTel();
            logger.info(message);

            logErreurRepository.save(new LogErreur(null,fileName,ligneActuelle,message,date));
            isValid = false;
        }



        if (isValid) {
            processedCins.add(personne.getCin());
            processedTels.add(personne.getTel());
            logger.info("✅ Succès : Personne traitée avec succès - CIN : " + personne.getCin());
            return personne;
        } else {
            return null;
        }

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
}