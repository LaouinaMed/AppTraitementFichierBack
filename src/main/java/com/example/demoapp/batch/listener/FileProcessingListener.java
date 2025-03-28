package com.example.demoapp.batch.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import java.nio.file.StandardCopyOption;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;

import java.util.logging.Level;
import java.util.logging.Logger;

@Slf4j
public class FileProcessingListener implements StepExecutionListener {

    private final String sourceDir;
    private final String validDirectory;
    private final String errorDirectory;

    //private static final Logger logger = Logger.getLogger(FileProcessingListener.class.getName());


    public FileProcessingListener(String sourceDir, String validDirectory, String errorDirectory) {
        this.sourceDir = sourceDir;
        this.validDirectory = validDirectory;
        this.errorDirectory = errorDirectory;

    }
/*
    @Override
    public void beforeStep(StepExecution stepExecution) {
        try {
            File sourceFolder = new File(sourceDir);
            File[] files = sourceFolder.listFiles((dir, name) -> name.endsWith(".txt"));
            if (files != null) {
                for (File file : files) {
                    Path destinationPath = Paths.get(validDirectory, file.getName());
                    if (Files.exists(destinationPath)) {
                        String newFileName = generateUniqueFileName(file.getName());
                        //moveFile(file, errorDirectory);
                        File newFile = new File(sourceDir, newFileName);
                         file.renameTo(newFile);

                    }
                }
            }

        } catch (Exception e) {
            log.info("Une erreur est survenue avant le traitement : " + e.getMessage());
        }
    }
*/

    private String generateUniqueFileName(String fileName) {
        String baseName = fileName.substring(0, fileName.lastIndexOf('.'));
        String extension = fileName.substring(fileName.lastIndexOf('.'));
        String uniqueName = baseName + "_" + System.currentTimeMillis() + extension;
        return uniqueName;
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        try {

            File sourceFolder = new File(sourceDir);

            File[] files = sourceFolder.listFiles((dir, name) -> name.endsWith(".txt"));
            File[] filesNotTxt = sourceFolder.listFiles((dir, name) -> !name.endsWith(".txt"));
            if (files != null) {
                for (File file : files) {

                    if (stepExecution.getStatus().isUnsuccessful() ) {
                        moveFile(file, errorDirectory);
                        log.info("**************afterStep");

                    } else {

                        Path destinationPath = Paths.get(validDirectory, file.getName());
                        if (Files.exists(destinationPath)) {
                            String newFileName = generateUniqueFileName(file.getName());
                            //moveFile(file, errorDirectory);
                            File newFile = new File(sourceDir, newFileName);
                            file.renameTo(newFile);
                        }
                        moveFile(file, validDirectory);

                    }
                }
            }

            if(filesNotTxt != null){
                for (File file : filesNotTxt) {
                    moveFile(file, errorDirectory);
                }
            }
            log.info("**************afterStep");

            return ExitStatus.COMPLETED;
        } catch (Exception e) {
            log.info("Une erreur est survenue : " + e.getMessage());
            return ExitStatus.FAILED;
        }
    }

    private void moveFile(File file, String destinationDir) {
        try {
            Path sourcePath = file.toPath();
            Path destinationPath = Paths.get(destinationDir, file.getName());
            File destinationDirectory = new File(destinationDir);

            if (!destinationDirectory.exists()) {
                destinationDirectory.mkdirs();
            }
            Files.move(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
            log.info(MessageFormat.format("✅ Succès: Le fichier a été déplacé avec succès : {0}" , destinationPath));


        } catch (Exception e) {
            log.info("Une erreur est survenue : " + e.getMessage());

        }
    }

}
