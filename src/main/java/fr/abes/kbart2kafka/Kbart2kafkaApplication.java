package fr.abes.kbart2kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.abes.kbart2kafka.dto.Header;
import fr.abes.kbart2kafka.dto.LigneKbartDto;
import fr.abes.kbart2kafka.exception.IllegalFileFormatException;
import fr.abes.kbart2kafka.exception.IllegalProviderException;
import fr.abes.kbart2kafka.kafka.TopicProducer;
import fr.abes.kbart2kafka.utils.CheckFiles;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.support.SendResult;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@EnableKafka
@SpringBootApplication
public class Kbart2kafkaApplication implements CommandLineRunner {

    @Value("${kbart.header}")
    private String kbartHeader;

    private final TopicProducer topicProducer;

    private final ObjectMapper mapper;

    ExecutorService executor = Executors.newFixedThreadPool(5);

    public Kbart2kafkaApplication(TopicProducer topicProducer, ObjectMapper mapper) {
        this.topicProducer = topicProducer;
        this.mapper = mapper;
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
    @Transactional
    // on spécifie la class qui fait rollback, par defaut c'est toutes les classes qui ne sont pas gérées càd : tout sauf IOException
    public void run(String... args) throws IOException {

        //	Contrôle de la présence d'un paramètre au lancement de Kbart2kafkaApplication
        if (args.length == 0 || args[0] == null || args[0].trim().isEmpty()) {
            log.error("Message envoyé : {}", "Le chemin d'accès au fichier tsv n'a pas été trouvé dans les paramètres de l'application");
        } else {
            log.info("Debut envois kafka de : " + args[0]);
            //	Récupération du chemin d'accès au fichier
            File tsvFile = new File(args[0]);

            try {
                //	Appelle du service de vérification de fichier
                CheckFiles.verifyFile(tsvFile, kbartHeader);

                // Calcul du nombre total de ligne
                Scanner kbartTotalLines = new Scanner(tsvFile);
                int totalNumberOfLine = 0;
                while (kbartTotalLines.hasNextLine()) {
                    String ligneKbart = kbartTotalLines.nextLine();
                    if (!ligneKbart.contains(kbartHeader)) {
                        totalNumberOfLine++;
                    }
                }

                // Compteur du nombre de lignes dans le kbart
                Scanner kbart = new Scanner(tsvFile);
                int lineCounter = 0;

                // Création du header et ajout du nombre total de lignes
                Header kafkaHeader = new Header(tsvFile.getName(), totalNumberOfLine);

                while (kbart.hasNextLine()) {
                    String ligneKbart = kbart.nextLine();
                    if (!ligneKbart.contains(kbartHeader)) {
                        lineCounter++;

                        // Crée un nouvel objet dto, set les différentes parties et envoi au service topicProducer
                        String[] tsvElementsOnOneLine = ligneKbart.split("\t");
                        LigneKbartDto ligneKbartDto = constructDto(tsvElementsOnOneLine);

                        //	Envoi de la ligne kbart dans le producer
                        kafkaHeader.setCurrentLine(lineCounter);

                        executor.submit(() -> {
                            try {
                                CompletableFuture<SendResult<String, String>> result = topicProducer.sendLigneKbart(ligneKbartDto, kafkaHeader);
                                log.debug("Message envoyé : {}", mapper.writeValueAsString(result.get().getProducerRecord().value()));
                            } catch (JsonProcessingException | InterruptedException | ExecutionException e) {
                                throw new RuntimeException(e);
                            }
                        });
                    }
                }
                // Envoi du message de fin de traitement dans le producer "OK"
                executor.submit(() -> topicProducer.sendOk(kafkaHeader));
            } catch (IOException e) {
                throw new IOException(e);
            } catch (IllegalFileFormatException | IllegalProviderException e) {
                throw new RuntimeException(e);
            }
            finally {
                executor.shutdown();
            }
        }
    }

    /**
     * Construction de la dto
     *
     * @param line ligne en entrée
     * @return Un objet DTO initialisé avec les informations de la ligne
     */
    private LigneKbartDto constructDto(String[] line) {
        LigneKbartDto kbartLineInDtoObject = new LigneKbartDto();
        kbartLineInDtoObject.setPublication_title(line[0]);
        kbartLineInDtoObject.setPrint_identifier(line[1]);
        kbartLineInDtoObject.setOnline_identifier(line[2]);
        kbartLineInDtoObject.setDate_first_issue_online(line[3]);
        kbartLineInDtoObject.setNum_first_vol_online(Integer.getInteger(line[4]));
        kbartLineInDtoObject.setNum_first_issue_online(Integer.getInteger(line[5]));
        kbartLineInDtoObject.setDate_last_issue_online(line[6]);
        kbartLineInDtoObject.setNum_last_vol_online(Integer.getInteger(line[7]));
        kbartLineInDtoObject.setNum_last_issue_online(Integer.getInteger(line[8]));
        kbartLineInDtoObject.setTitle_url(line[9]);
        kbartLineInDtoObject.setFirst_author(line[10]);
        kbartLineInDtoObject.setTitle_id(line[11]);
        kbartLineInDtoObject.setEmbargo_info(line[12]);
        kbartLineInDtoObject.setCoverage_depth(line[13]);
        kbartLineInDtoObject.setNotes(line[14]);
        kbartLineInDtoObject.setPublisher_name(line[15]);
        kbartLineInDtoObject.setPublication_type(line[16]);
        kbartLineInDtoObject.setDate_monograph_published_print(line[17]);
        kbartLineInDtoObject.setDate_monograph_published_online(line[18]);
        kbartLineInDtoObject.setMonograph_volume(Integer.getInteger(line[19]));
        kbartLineInDtoObject.setMonograph_edition(line[20]);
        kbartLineInDtoObject.setFirst_editor(line[21]);
        kbartLineInDtoObject.setParent_publication_title_id(line[22]);
        kbartLineInDtoObject.setPreceding_publication_title_id(line[23]);
        kbartLineInDtoObject.setAccess_type(line[24]);
        // Vérification de la présence d'un best ppn déjà renseigné dans le kbart
        if (line.length == 26) {
            kbartLineInDtoObject.setBestPpn(line[25]);
        }
        return kbartLineInDtoObject;
    }
}
