package fr.abes.kafkaconvergence.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.abes.kafkaconvergence.dto.LigneKbartDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TopicProducer {

    @Value("${topic.name}")
    private String topicName;

    private final KafkaTemplate<String, String> kafkaTemplate;

    private final ObjectMapper mapper;

    public void sendKbart(LigneKbartDto kbart) throws JsonProcessingException {
        log.info("Message envoyé : {}", mapper.writeValueAsString(kbart));
        kafkaTemplate.send(topicName, Integer.valueOf(kbart.hashCode()).toString(), mapper.writeValueAsString(kbart));
    }

    public void sendPrintNotice(String ppn, LigneKbartDto kbart, String provider) {

    }
}
