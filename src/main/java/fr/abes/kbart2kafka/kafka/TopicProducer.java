package fr.abes.kbart2kafka.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.abes.kbart2kafka.dto.Header;
import fr.abes.kbart2kafka.dto.LigneKbartDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;
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

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    private final ObjectMapper mapper;

    /**
     * Envoi une ligne d'un kbart dans un topic kafka
     * @param kbart un fichier kbart
     * @param header le nom du fichier avec son extension, la date, le nombre de ligne du kbart
     * @throws JsonProcessingException Exception pour tous les problèmes de traitement de contenu JSON
     */
    public void sendKbart(LigneKbartDto kbart, Header header) throws JsonProcessingException {

        Message<String> message = MessageBuilder
                .withPayload(mapper.writeValueAsString(kbart))
                .setHeader(KafkaHeaders.TOPIC, topicKbart)
                .setHeader(header.getFileName(), header.getCurrentLine() + "/" + header.getTotalNumberOfLine())
                .build();

        kafkaTemplate.send(message);

//        // Création du header
//        List<RecordHeader> headers = new ArrayList<>();
//        headers.add(new RecordHeader(header.getFileName(), String.valueOf(header.getCurrentLine() + "/" + header.getTotalNumberOfLine()).getBytes()));
//
//        // Création du message et envoi
//        ProducerRecord<String, String> message1 = new ProducerRecord<>(topicKbart, 0, "", kbart, headers);
//        kafkaTemplate.send(message1);

        // Log
        log.debug("Message envoyé : {}", mapper.writeValueAsString(kbart));
    }

    /**
     * Envoi le message de fin de traitement d'un kbart dans un topic kafka
     * @param header le nom du fichier avec son extension, la date, le nombre de ligne du kbart
     */
    public void sendKbart(Header header) {

        Message<String> message = MessageBuilder
                .withPayload("OK")
                .setHeader(KafkaHeaders.TOPIC, topicKbart)
                .setHeader(header.getFileName(), header.getCurrentLine() + "/" + header.getTotalNumberOfLine())
                .build();

        kafkaTemplate.send(message);


//        // Création du header
//        List<RecordHeader> headers = new ArrayList<>();
//        headers.add(new RecordHeader(header.getFileName(), String.valueOf(header.getCurrentLine() + "/" + header.getTotalNumberOfLine()).getBytes()));
//
//        // Création du message et envoi
//        ProducerRecord<String, String> message1 = new ProducerRecord<>(topicKbart, 0, "", "Ok", headers);
//
//        kafkaTemplate.send(message1);

        // Log
        log.debug("Message envoyé : {}", "Le kbart " + header + " a été traité dans son intégralité.");
    }
}
