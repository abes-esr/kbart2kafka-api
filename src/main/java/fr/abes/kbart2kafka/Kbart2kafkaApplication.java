package fr.abes.kbart2kafka;

import fr.abes.kbart2kafka.exception.IllegalFileFormatException;
import fr.abes.kbart2kafka.exception.IllegalProviderException;
import fr.abes.kbart2kafka.service.FileService;
import fr.abes.kbart2kafka.utils.CheckFiles;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

@Slf4j
@SpringBootApplication
public class Kbart2kafkaApplication implements CommandLineRunner {
    @Value("${kbart.header}")
    private String kbartHeader;
    private final FileService service;

    public Kbart2kafkaApplication(FileService service) {
        this.service = service;
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
            log.info("Debut envois kafka de : " + args[0]);
            //	Récupération du chemin d'accès au fichier
            File tsvFile = new File(args[0]);
            //	Appelle du service de vérification de fichier
            try {
                CheckFiles.verifyFile(tsvFile, kbartHeader);
            } catch (IllegalFileFormatException | IllegalProviderException e) {
                throw new RuntimeException(e);
            }
            service.loadFile(tsvFile, kbartHeader);
        }
        long endTime = System.currentTimeMillis();
        double executionTime = (double) (endTime - startTime) / 1000;
        log.info("Temps d'exécution : " + executionTime + " secondes");
    }


}
