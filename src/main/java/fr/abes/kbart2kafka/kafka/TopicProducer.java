package fr.abes.kbart2kafka.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.abes.kbart2kafka.dto.LigneKbartDto;
import fr.abes.kbart2kafka.dto.PackageKbartDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TopicProducer {

    @Value("${topic.name.target.kbartt}")
    private String topicKbart;

    private final KafkaTemplate<String, String> kafkaTemplate;

    private final ObjectMapper mapper;

    public void sendKbart(LigneKbartDto kbart) throws JsonProcessingException {
        log.debug("Message envoyé : {}", mapper.writeValueAsString(kbart));
        String fileName = ThreadContext.get("package");
        kafkaTemplate.send(topicKbart, fileName, mapper.writeValueAsString(kbart));
    }

    public void sendKbart(PackageKbartDto kbart) throws JsonProcessingException {
        log.debug("Message envoyé : {}", mapper.writeValueAsString(kbart));
        String fileName = ThreadContext.get("package");
        kafkaTemplate.send(topicKbart, fileName, mapper.writeValueAsString(kbart));
    }
}
