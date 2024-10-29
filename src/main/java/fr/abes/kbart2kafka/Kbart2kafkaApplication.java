package fr.abes.kbart2kafka;

import fr.abes.kbart2kafka.exception.IllegalDateException;
import fr.abes.kbart2kafka.exception.IllegalPackageException;
import fr.abes.kbart2kafka.exception.IllegalProviderException;
import fr.abes.kbart2kafka.repository.ProviderRepository;
import fr.abes.kbart2kafka.service.FileService;
import fr.abes.kbart2kafka.service.ProviderPackageService;
import fr.abes.kbart2kafka.utils.CheckFiles;
import fr.abes.kbart2kafka.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.io.IOException;

@Slf4j
@SpringBootApplication
public class Kbart2kafkaApplication implements CommandLineRunner {
    @Value("${kbart.header}")
    private String kbartHeader;
    private final FileService service;

    private final ProviderPackageService providerPackageService;

    private final ProviderRepository providerRepository;

    public Kbart2kafkaApplication(FileService service, ProviderPackageService providerPackageService, ProviderRepository providerRepository) {
        this.service = service;
        this.providerPackageService = providerPackageService;
        this.providerRepository = providerRepository;
    }

    public static void main(String[] args) {
        SpringApplication.run(Kbart2kafkaApplication.class, args);
    }

    /**
     * Méthode run qui lit un fichier tsv et en extrait les données ligne par ligne pour les envoyer dans un topic kafka
     *
     * @param args fichier tsv placé dans un répertoire défini
     * @throws IOException Exception levée lorsque aucun fichier tsv n'a été trouvé.
     */
    @Override
    public void run(String... args) throws IOException {
        long startTime = System.currentTimeMillis();
        //	Contrôle de la présence d'un paramètre au lancement de Kbart2kafkaApplication
        if (args.length == 0 || args[0] == null || args[0].trim().isEmpty()) {
            log.error("Message envoyé : {}", "Le chemin d'accès au fichier tsv n'a pas été trouvé dans les paramètres de l'application");
        } else {
            String fileName = Utils.extractFilename(args[0]);
            ThreadContext.put("package", fileName);
            log.info("Debut envois kafka de : " + fileName);
            //	Récupération du chemin d'accès au fichier
            File tsvFile = new File(args[0]);
            try {
                CheckFiles.verifyFile(tsvFile, kbartHeader);
                checkExistingPackage(tsvFile.getName());
                service.loadFile(tsvFile);
            } catch (Exception | IllegalPackageException e) {
                log.error(e.getMessage());
                log.info("Traitement refusé du fichier " + tsvFile.getName());
            } finally {
                File fichierLog = new File(tsvFile.getPath().replace(".tsv",".log"));
                if(fichierLog.createNewFile())
                    log.debug("Création du fichier " + fichierLog.getName());
            }
        }
        long endTime = System.currentTimeMillis();
        double executionTime = (double) (endTime - startTime) / 1000;
        log.debug("Temps d'exécution : " + executionTime + " secondes");
    }

    private void checkExistingPackage(String filename) throws IllegalProviderException, IllegalPackageException, IllegalDateException {
        if (providerPackageService.hasMoreRecentPackageInBdd(Utils.extractProvider(filename), Utils.extractPackageName(filename), Utils.extractDateFilename(filename)))
            throw new IllegalPackageException("Un package plus récent est déjà présent dans la base");
    }
}
