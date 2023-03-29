package fr.abes.kafkaconvergence.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TopicProducerLogger {

    @Value("${topic.errorname}")
    private String topicNameError;

    @Value("${topic.infoname}")
    private String topicNameInfo;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public void sendError(String key, String message){
        log.debug("Message envoyé d'erreur: {}", message);
        kafkaTemplate.send(topicNameError, key, message);
    }

    public void sendInfo(String key, String message){
        log.debug("Message envoyé d'info: {}", message);
        kafkaTemplate.send(topicNameInfo, key, message);
    }
}
