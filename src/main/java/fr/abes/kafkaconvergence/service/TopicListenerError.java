package fr.abes.kafkaconvergence.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.abes.kafkaconvergence.dto.ErreurResultDto;
import fr.abes.kafkaconvergence.dto.LigneKbartDto;
import fr.abes.kafkaconvergence.entity.ErreurResult;
import fr.abes.kafkaconvergence.entity.LigneKbart;
import fr.abes.kafkaconvergence.repository.ErreurResultDao;
import fr.abes.kafkaconvergence.utils.UtilsMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class TopicListenerError {
    @Value("${topic.errorname}")
    private String topicName;

    private final UtilsMapper mapper;

    private final ObjectMapper jacksonMapper;

    private final ErreurResultDao service;

    @KafkaListener(topics = "${topic.errorname}", groupId = "convergence")
    public void consume(ConsumerRecord<String, String> payload) throws JsonProcessingException {
        //log.info("Topic : {}", topicName);
        log.info("Topic name : " + topicName);
        log.info("payload info" + payload.key());
        //log.info("Headers : {}", payload.headers());
        //log.info("Partition : {}", payload.partition());
        log.info("payload value" + payload.value());
        ErreurResultDto erreurResultDto = jacksonMapper.readValue(payload.value(), ErreurResultDto.class);

        service.save(mapper.map(erreurResultDto, ErreurResult.class));

    }
}