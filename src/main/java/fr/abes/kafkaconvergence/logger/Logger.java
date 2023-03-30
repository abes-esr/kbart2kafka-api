package fr.abes.kafkaconvergence.logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.abes.kafkaconvergence.dto.LoggerResultDto;
import fr.abes.kafkaconvergence.service.TopicProducerLogger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class Logger {

    private final TopicProducerLogger topicProducerLogger;

    private final ObjectMapper mapper;

    public void error(String key, LoggerResultDto messageError) throws JsonProcessingException {
        this.topicProducerLogger.sendError(key, mapper.writeValueAsString(messageError));
    }

    public void info(String key, LoggerResultDto messageInfo) throws JsonProcessingException {
        this.topicProducerLogger.sendInfo(key, mapper.writeValueAsString(messageInfo));
    }
}

