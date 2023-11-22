package fr.abes.kbart2kafka.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.abes.kbart2kafka.dto.Header;
import fr.abes.kbart2kafka.dto.LigneKbartDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@Slf4j
public class FileService {

    @Value("${topic.name.target.kbart}")
    private String topicKbart;

    private final KafkaTemplate<String, String> kafkaTemplate;

    private final ObjectMapper mapper;
    ExecutorService executor = Executors.newFixedThreadPool(5);

    public FileService(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper mapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.mapper = mapper;
    }

    @Transactional
    public void loadFile(File fichier, String kbartHeader, long totalNumberOfLine) {
        try {
            executeMultiThread(fichier, kbartHeader, totalNumberOfLine);
        } catch (IOException ex) {
            log.error("Erreur dans la lecture du fichier");
        }

    }

    private void executeMultiThread(File fichier, String kbartHeader, long totalNumberOfLine) throws IOException {
        // Compteur du nombre de lignes dans le kbart
        int lineCounter = 0;
        // Création du header et ajout du nombre total de lignes
        Header kafkaHeader = new Header(fichier.getName(), totalNumberOfLine);
        BufferedReader buff = new BufferedReader(new FileReader(fichier));
        for (String ligneKbart : buff.lines().toList()) {
            if (!ligneKbart.contains(kbartHeader)) {
                lineCounter++;
                // Crée un nouvel objet dto, set les différentes parties et envoi au service topicProducer
                String[] tsvElementsOnOneLine = ligneKbart.split("\t");
                LigneKbartDto ligneKbartDto = constructDto(tsvElementsOnOneLine);

                //	Envoi de la ligne kbart dans le producer
                kafkaHeader.setCurrentLine(lineCounter);
                executor.execute(() -> {
                    try {
                        List<org.apache.kafka.common.header.Header> headers = new ArrayList<>();
                        headers.add(new RecordHeader("FileName", kafkaHeader.getFileName().getBytes(StandardCharsets.UTF_8)));
                        headers.add(new RecordHeader("CurrentLine", String.valueOf(kafkaHeader.getCurrentLine()).getBytes(StandardCharsets.UTF_8)));
                        headers.add(new RecordHeader("TotalLine", String.valueOf(kafkaHeader.getTotalNumberOfLine()).getBytes(StandardCharsets.UTF_8)));
                        ProducerRecord<String, String> record = new ProducerRecord<>(topicKbart, new Random().nextInt(5), "", mapper.writeValueAsString(ligneKbartDto), headers);
                        CompletableFuture<SendResult<String, String>> result = kafkaTemplate.executeInTransaction(kt -> kt.send(record));
                        log.debug("Message envoyé : {}", mapper.writeValueAsString(result.get().getProducerRecord().value()));
                    } catch (JsonProcessingException | ExecutionException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        }
        executor.shutdown();
        Message<String> message = MessageBuilder
                .withPayload("OK")
                .setHeader(KafkaHeaders.TOPIC, topicKbart)
                .setHeader("FileName", kafkaHeader.getFileName())
                .setHeader("CurrentLine", kafkaHeader.getCurrentLine())
                .setHeader("TotalLine", kafkaHeader.getTotalNumberOfLine())
                .build();

        kafkaTemplate.send(message);
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
