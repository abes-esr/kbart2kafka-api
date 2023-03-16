package fr.abes.kafkaconvergence.controller;

import fr.abes.kafkaconvergence.service.TopicProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@CrossOrigin(origins = "*")
@Slf4j
@RequiredArgsConstructor
@RestController
public class KafkaController {
    private final TopicProducer topicProducer;

    @RequestMapping("/insertKbartLigneFromFile")
    public ResponseEntity<String> sruUpdate() {
        HttpHeaders responseHeaders = new HttpHeaders();
        String reponse = "vide";
        topicProducer.send("er", "ey");
        return new ResponseEntity<String>(reponse, responseHeaders, HttpStatus.OK);
    }
}
