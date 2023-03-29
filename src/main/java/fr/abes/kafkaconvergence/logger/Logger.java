package fr.abes.kafkaconvergence.logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.abes.kafkaconvergence.service.TopicProducerLogger;
import lombok.RequiredArgsConstructor;



@RequiredArgsConstructor
public class Logger {

    private TopicProducerLogger topicProducerLogger;

    private final ObjectMapper mapper;

    public void error(String key, Object messageError) throws JsonProcessingException {
        this.topicProducerLogger.sendError(key, mapper.writeValueAsString(messageError));
    }

    public void warn(String key, Object messageError) {

    }
}

