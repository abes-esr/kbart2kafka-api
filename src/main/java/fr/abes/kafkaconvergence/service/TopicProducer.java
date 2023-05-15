package fr.abes.kafkaconvergence.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.abes.kafkaconvergence.dto.LigneKbartDto;
import fr.abes.kafkaconvergence.dto.PpnKbartProviderDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TopicProducer {

    @Value("${topic.name.kbart}")
    private String topicKbart;

    @Value("${topic.name.ppnKbartProvider}")
    private String topicPpnKbartProvider;

    private final KafkaTemplate<String, String> kafkaTemplate;

    private final ObjectMapper mapper;

    public void sendKbart(LigneKbartDto kbart) throws JsonProcessingException {
        log.debug("Message envoyé : {}", mapper.writeValueAsString(kbart));
        kafkaTemplate.send(topicKbart, Integer.valueOf(kbart.hashCode()).toString(), mapper.writeValueAsString(kbart));
    }

    public void sendPrintNotice(String ppn, LigneKbartDto kbart, String provider) throws JsonProcessingException {
        PpnKbartProviderDto dto = new PpnKbartProviderDto(ppn, kbart, provider);
        log.debug("Message envoyé : {}", dto);
        kafkaTemplate.send(topicPpnKbartProvider, Integer.valueOf(dto.hashCode()).toString(), mapper.writeValueAsString(dto));
    }
}
