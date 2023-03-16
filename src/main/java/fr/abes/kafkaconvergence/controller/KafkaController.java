package fr.abes.kafkaconvergence.controller;

import fr.abes.kafkaconvergence.service.TopicProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@CrossOrigin(origins = "*")
@Slf4j
@RequiredArgsConstructor
@RestController
public class KafkaController {
    private final TopicProducer topicProducer;

    @PostMapping("/kbart2Kafka")
    public void kbart2kafka(MultipartFile file) throws IOException {
        //TODO controle format fichier (extension)
        isTSV(file);
        //TODO controle presence entete fichier
        //TODO lecture fichier, ligne par ligne, creation objet java pour chaque ligne
        //TODO injection de chaque ligne dans kafka (cle = id genere ad hoc par appli, valeur = objet java)
        //TODO utiliser comme cle de chaque ligne inserer dans kafka un hashcode de l'objet
        String reponse = "vide";
        topicProducer.send("err", "ey");
    }

    private boolean isTSV(MultipartFile file) throws IOException {
        //Filename extension control
        String fileName = file.getOriginalFilename(); // get file name
        if (fileName == null || fileName.isEmpty()) return false; // check if file name is valid
        String[] parts = fileName.split("\\."); // split by dot
        String extension = parts[parts.length - 1]; // get last part as extension

        //File MIME Type control
        Tika tika = new Tika(); // create a new instance of Tika
        String contentType = tika.detect(file.getInputStream(), file.getOriginalFilename()); // detect the content type based on the input stream and the file name
        //TODO apache tika renvoie actuellement un format text/plain avec un fichier tsv auquel on a retiré l'extension tsv, tester avec insomnia (voir slack), il faut que tika.detect retourne un text/tab-separated-values

        return extension.equalsIgnoreCase("tsv") || contentType.equalsIgnoreCase("text/tab-separated-values"); // compare with tsv ignoring case
    }
}
