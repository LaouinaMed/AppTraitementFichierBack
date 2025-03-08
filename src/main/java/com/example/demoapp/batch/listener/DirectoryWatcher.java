package com.example.demoapp.batch.listener;

import com.example.demoapp.batch.processor.PersonneProcessor;
import com.example.demoapp.entities.Personne;
import com.example.demoapp.repositories.LogErreurRepository;
import com.example.demoapp.repositories.PersonneRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.batch.core.*;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;

import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionException;

import java.io.IOException;
import java.nio.file.*;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

@Component
public class DirectoryWatcher {

    private final JobLauncher jobLauncher;
    private final JobRepository jobRepository; //meta donnees etat d'un job
    private final PlatformTransactionManager platformTransactionManager; //gerer les tensactions
    private final PersonneRepository personneRepository;
    private final LogErreurRepository logErreurRepository;

    private  PersonneProcessor processor;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private static final Logger logger = Logger.getLogger(DirectoryWatcher.class.getName());


    @Autowired
    public DirectoryWatcher(JobLauncher jobLauncher, JobRepository jobRepository, PlatformTransactionManager platformTransactionManager, PersonneRepository repository,LogErreurRepository logErreurRepository) {
        this.jobLauncher = jobLauncher;
        this.jobRepository = jobRepository;
        this.platformTransactionManager = platformTransactionManager;
        this.personneRepository = repository;
        this.logErreurRepository =logErreurRepository;

    }

    @PostConstruct
    public void startWatching() {
        executorService.submit(() -> {
            try {
                Path directoryPath = Paths.get("C:/Users/simed/Desktop/ReaderBatch");
                watchDirectory(directoryPath);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.severe("Le thread de surveillance a été interrompu : " + e.getMessage());
            } catch (Exception e) {
                logger.severe("Une erreur est survenue : " + e.getMessage());
            }
        });
    }

    @EventListener
    public void watchDirectory(Path dirPath) throws IOException, InterruptedException {
        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
            dirPath.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);

            try (Stream<Path> stream = Files.list(dirPath)) {  //  Fermeture automatique du stream
                stream.filter(Files::isRegularFile)
                        .forEach(file -> {
                            logger.info("Fichier déjà présent : " + file.getFileName());
                            logger.info("Job lancé après présence du fichier");

                            launchJobForFile(file.getFileName().toString());
                        });
            } catch (IOException e) {
                logger.severe("Erreur lors de la lecture du dossier : " + e.getMessage());
            }

            logger.info("***** Start watching directory *****");

            while (true) {
                WatchKey key = watchService.take();
                for (WatchEvent<?> event : key.pollEvents()) {
                    if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {

                        logger.info("Job lancé après la détection de fichier");

                        try {
                            launchJobForFile(event.context().toString());
                        } catch (Exception e) {
                            logger.severe("Une erreur est survenue : " + e.getMessage());
                        }
                    }
                }

                boolean valid = key.reset();
                if (!valid) {
                    break;
                }
            }
        }

    }

    private Job createJob() throws IOException, TransactionException {
        logger.info("**************Job - Start");

        return new JobBuilder("importPersonnes", jobRepository)
                .start(importStep())
                .build();
    }

    private Step importStep() throws IOException {
        logger.info("**************import Step - Start");

        return new StepBuilder("txtImport", jobRepository)
                .<Personne, Personne>chunk(2, platformTransactionManager)
                .reader(multiResourceItemReader())
                .processor(processor())
                .writer(writer())
                .listener(new FileProcessingListener(
                        "C:/Users/simed/Desktop/ReaderBatch",
                        "C:/Users/simed/Desktop/FichierValide",
                        "C:/Users/simed/Desktop/FichierNonValide"))
                .build();
    }





    public FlatFileItemReader<Personne> itemReader(){
        FlatFileItemReader<Personne> itemReader = new FlatFileItemReader<>();
        logger.info("**************Reading ItemReader");

        itemReader.setName("txtReader");
        itemReader.setLinesToSkip(1);
        itemReader.setLineMapper(lineMapper()); //convertir chaque ligne un objet java
        itemReader.setStrict(false);
        return itemReader;
    }

    public MultiResourceItemReader<Personne> multiResourceItemReader() throws IOException {
        MultiResourceItemReader<Personne> multiResourceItemReader = new MultiResourceItemReader<>();
        logger.info("**************Reader");

        Resource[] resources = new PathMatchingResourcePatternResolver()
                .getResources("file:C:/Users/simed/Desktop/ReaderBatch/*.txt");

        multiResourceItemReader.setResources(resources);
        multiResourceItemReader.setDelegate(itemReader());

        return multiResourceItemReader;
    }

    private LineMapper<Personne> lineMapper(){
        DefaultLineMapper<Personne> lineMapper = new DefaultLineMapper<>();

        DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
        lineTokenizer.setDelimiter("|");
        lineTokenizer.setStrict(false);
        lineTokenizer.setNames("cin","prenom","nom","tel","adresse");

        BeanWrapperFieldSetMapper<Personne> fieldSetMapper =new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(Personne.class);

        lineMapper.setLineTokenizer(lineTokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);

        return lineMapper;
    }

    public PersonneProcessor processor(){
        logger.info("**************Processing: " );
        return new PersonneProcessor(personneRepository,logErreurRepository);
    }

    public RepositoryItemWriter<Personne> writer(){
        RepositoryItemWriter<Personne> writer = new RepositoryItemWriter<>();
        logger.info("**************Writer");

        writer.setRepository(personneRepository);
        writer.setMethodName("save");

        return writer;
    }

    private void launchJobForFile(String fileName) {
        long timestamp = System.currentTimeMillis(); // 🔥 Capture du time
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = sdf.format(new Date(timestamp));
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("fileName", fileName)
                .addLong("timestamp", System.currentTimeMillis())
                .addString("formattedDate", formattedDate)
                .toJobParameters();
        try {
            if (logger.isLoggable(Level.INFO)) {
                logger.info(MessageFormat.format("✅ Le fichier a été déplacé sous un nouveau nom : {0}" , fileName));
            }
            Job job = createJob(); // Crée le job ici aussi
            jobLauncher.run(job, jobParameters);
        } catch (Exception e) {
            logger.severe("erreur est survenue : " + e.getMessage());

        }
    }
}
