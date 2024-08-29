package fr.abes.kbart2kafka.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.abes.kbart2kafka.configuration.KafkaConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;

@SpringBootTest(classes = {FileService.class, KafkaConfig.class, ObjectMapper.class})
public class FileServiceTest {

    @Autowired
    FileService fileService;
    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;
    @Autowired
    ObjectMapper objectMapper;

    @Test
    void testCalculatePartition() {
        Assertions.assertEquals(0, fileService.calculatePartition(2));
        Assertions.assertEquals(1, fileService.calculatePartition(2));
        Assertions.assertEquals(0, fileService.calculatePartition(2));
    }

}
