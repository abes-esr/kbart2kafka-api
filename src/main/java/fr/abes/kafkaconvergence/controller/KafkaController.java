package fr.abes.kafkaconvergence.controller;

import fr.abes.kafkaconvergence.service.TopicProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@CrossOrigin(origins = "*")
@Slf4j
@RequiredArgsConstructor
@RestController
public class KafkaController {
    private final TopicProducer topicProducer;

    @PostMapping("/kbart2Kafka")
    public void kbart2kafka(MultipartFile file) {
        //TODO controle format fichier (extension)
        //TODO controle presence entete fichier
        //TODO lecture fichier, ligne par ligne, creation objet java pour chaque ligne
        //TODO injection de chaque ligne dans kafka (cle = id genere ad hoc par appli, valeur = objet java)
        //TODO utiliser comme cle de chaque ligne inserer dans kafka un hashcode de l'objet
        String reponse = "vide";
        topicProducer.send("err", "ey");
    }
}
