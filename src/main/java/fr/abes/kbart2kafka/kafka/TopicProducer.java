package fr.abes.kbart2kafka.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.abes.kbart2kafka.dto.LigneKbartDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Producer Kafka qui envoi les lignes d'un kbart et les messages de fin de traitement dans un topic kafka
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TopicProducer {

    @Value("${topic.name.target.kbart}")
    private String topicKbart;

    private final KafkaTemplate<String, String> kafkaTemplate;

    private final ObjectMapper mapper;

    /**
     * Envoi une ligne d'un kbart dans un topic kafka
     * @param kbart un fichier kbart
     * @param kbartName le nom du fichier (sans son extension)
     * @param totalNumberOfLine nombre total de lignes dans le fichier kbart (excepté la première ligne d'en-tête)
     * @param lineNumber nombre de la ligne courante
     * @throws JsonProcessingException Exception pour tous les problèmes de traitement de contenu JSON
     */
    public void sendKbart(LigneKbartDto kbart, String kbartName, int totalNumberOfLine, int lineNumber) throws JsonProcessingException {
        // Création du header
        List<Header> headers = new ArrayList<>();
        String totalLine = totalNumberOfLine + " lignes.";
        headers.add(new RecordHeader("ligne " + lineNumber + " sur ", totalLine.getBytes()));

        // Création du message et envoi
        ProducerRecord<String, String> message = new ProducerRecord<>(topicKbart, 0, kbartName, mapper.writeValueAsString(kbart), headers);
        kafkaTemplate.send(message);

        // Log
        log.debug("Message envoyé : {}", mapper.writeValueAsString(kbart));
    }

    /**
     * Envoi le message de fin de traitement d'un kbart dans un topic kafka
     * @param kbartName le nom du fichier (sans son extension)
     */
    public void sendKbart(String kbartName) {
        // Création du message et envoi
        ProducerRecord<String, String> message = new ProducerRecord<>(topicKbart, 0, kbartName, "OK");
        kafkaTemplate.send(message);

        // Log
        log.debug("Message envoyé : {}", "Le kbart " + kbartName + " a été traité dans son intégralité.");
    }
}
