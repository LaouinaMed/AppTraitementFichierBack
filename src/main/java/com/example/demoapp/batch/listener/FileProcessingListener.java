package com.example.demoapp.batch.listener;

import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;

import java.util.logging.Level;
import java.util.logging.Logger;

public class FileProcessingListener implements StepExecutionListener {

    private final String sourceDir;
    private final String validDirectory;
    private final String errorDirectory;

    private static final Logger logger = Logger.getLogger(FileProcessingListener.class.getName());


    public FileProcessingListener(String sourceDir, String validDirectory, String errorDirectory) {
        this.sourceDir = sourceDir;

        this.validDirectory = validDirectory;
        this.errorDirectory = errorDirectory;

    }


    @Override
    public void beforeStep(StepExecution stepExecution) {
        try {
            File sourceFolder = new File(sourceDir);
            File[] files = sourceFolder.listFiles((dir, name) -> name.endsWith(".txt"));
            if (files != null) {
                for (File file : files) {
                    Path destinationPath = Paths.get(validDirectory, file.getName());
                    if (Files.exists(destinationPath)) {
                        moveFile(file, errorDirectory);
                    }
                }
            }

        } catch (Exception e) {
            logger.severe("Une erreur est survenue avant le traitement : " + e.getMessage());
        }
    }




    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        try {

            File sourceFolder = new File(sourceDir);

            File[] files = sourceFolder.listFiles((dir, name) -> name.endsWith(".txt")); // Filtrer pour ne prendre que les .txt
            File[] filesNotTxt = sourceFolder.listFiles((dir, name) -> !name.endsWith(".txt"));
            if (files != null) {
                for (File file : files) {

                    if (stepExecution.getStatus().isUnsuccessful() ) {
                        moveFile(file, errorDirectory);
                    } else {
                        moveFile(file, validDirectory);
                    }
                }
            }

            if(filesNotTxt != null){
                for (File file : filesNotTxt) {
                    moveFile(file, errorDirectory);
                }
            }
            logger.info("**************afterStep");

            return ExitStatus.COMPLETED;
        } catch (Exception e) {
            logger.severe("Une erreur est survenue : " + e.getMessage());
            return ExitStatus.FAILED;
        }
    }

    private void moveFile(File file, String destinationDir) {
        try {
            Path sourcePath = file.toPath();
            Path destinationPath = Paths.get(destinationDir, file.getName());
            File destinationDirectory = new File(destinationDir);

            if (!destinationDirectory.exists()) {
                destinationDirectory.mkdirs(); // Créer les répertoires si nécessaires
            }


            Files.move(sourcePath, destinationPath);
            if (logger.isLoggable(Level.INFO)) {
                logger.info(MessageFormat.format("✅ Succès: Le fichier a été déplacé avec succès : {0}" , destinationPath));
            }

        } catch (Exception e) {
            logger.severe("Une erreur est survenue : " + e.getMessage());

        }
    }

}
