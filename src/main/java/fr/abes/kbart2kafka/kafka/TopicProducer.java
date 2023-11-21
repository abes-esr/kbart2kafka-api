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
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

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
     *
     * @param kbart  un fichier kbart
     * @param header le nom du fichier avec son extension, la date, le nombre de ligne du kbart
     * @return
     * @throws JsonProcessingException Exception pour tous les problèmes de traitement de contenu JSON
     */
    public void sendLigneKbart(LigneKbartDto kbart, Header header) throws JsonProcessingException {

        List<org.apache.kafka.common.header.Header> headers = new ArrayList<>();
        headers.add(new RecordHeader("FileName", header.getFileName().getBytes(StandardCharsets.UTF_8)));
        headers.add(new RecordHeader("CurrentLine", String.valueOf(header.getCurrentLine()).getBytes(StandardCharsets.UTF_8)));
        headers.add(new RecordHeader("TotalLine", String.valueOf(header.getTotalNumberOfLine()).getBytes(StandardCharsets.UTF_8)));
        ProducerRecord<String, String> record = new ProducerRecord<>(topicKbart, new Random().nextInt(5), "", mapper.writeValueAsString(kbart), headers);

//        Message<String> message = MessageBuilder
//                .withPayload(mapper.writeValueAsString(kbart))
//                .setHeader(KafkaHeaders.TOPIC, topicKbart)
//                .setHeader("FileName", header.getFileName())
//                .setHeader("CurrentLine", header.getCurrentLine())
//                .setHeader("TotalLine", header.getTotalNumberOfLine())
//                .build();
        kafkaTemplate.send(record);
    }

    /**
     * Envoi le message de fin de traitement d'un kbart dans un topic kafka
     *
     * @param header le nom du fichier avec son extension, la date, le nombre de ligne du kbart
     * @return
     */
    public CompletableFuture<SendResult<String, String>> sendOk(Header header) {

        Message<String> message = MessageBuilder
                .withPayload("OK")
                .setHeader(KafkaHeaders.TOPIC, topicKbart)
                .setHeader("FileName", header.getFileName())
                .setHeader("CurrentLine", header.getCurrentLine())
                .setHeader("TotalLine", header.getTotalNumberOfLine())
                .build();

        return kafkaTemplate.send(message);
    }
}
