package fr.abes.kafkaconvergence.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class TopicListener {
    @Value("${topic.name")
    private String topicName;

    @KafkaListener(topics = "${topic.name}")
    public void consume(ConsumerRecord<String, String> payload){
            //log.info("Topic : {}", topicName);
            log.info("payload info", payload.key());
            //log.info("Headers : {}", payload.headers());
            //log.info("Partition : {}", payload.partition());
            log.info("payload value", payload.value());
        }
}