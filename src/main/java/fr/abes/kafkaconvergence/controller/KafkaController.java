package fr.abes.kafkaconvergence.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.abes.kafkaconvergence.dto.LigneKbartDto;
import fr.abes.kafkaconvergence.dto.ResultWsSudocDto;
import fr.abes.kafkaconvergence.service.TopicProducer;
import fr.abes.kafkaconvergence.service.WsService;
import fr.abes.kafkaconvergence.utils.CheckFiles;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

@CrossOrigin(origins = "*")
@Slf4j
@RequiredArgsConstructor
@RestController
public class KafkaController {
    private final TopicProducer topicProducer;
    private final WsService service;
    private final ObjectMapper mapper;

    @PostMapping("/kbart2Kafka")
    public void kbart2kafka(MultipartFile file) throws IOException {
        //execution seulement si:
        //le fichier à une extension tsv,
        //contient des tabulations,
        //contient un entête avec la présence du terme publication title
        if (CheckFiles.isFileWithTSVExtension(file) && CheckFiles.detectTabulations(file) && CheckFiles.detectOfHeaderPresence("publication_title", file)) {
            //lecture fichier, ligne par ligne, creation objet java pour chaque ligne
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.contains("publication_title")) {
                        String[] tsvElementsOnOneLine = line.split("\t");
                        // Crée un nouvel objet dto et set les différentes parties
                        LigneKbartDto kbart = constructDto(tsvElementsOnOneLine);
                        ResultWsSudocDto result = service.callOnlineId2Ppn(kbart.getPublication_type(), kbart.getOnline_identifier());
                        if (result.getPpns().size() > 0) {
                            kbart.setBestPpn(result.getPpns().get(0).getPpn());
                            log.info(String.valueOf(kbart.hashCode()));
                            topicProducer.send(Integer.valueOf(kbart.hashCode()).toString(), mapper.writeValueAsString(kbart));
                        }
                    }
                }
            }
        }
    }



    /**
     * Construction de la dto
     * @param line ligne en entrée
     * @return Un objet DTO initialisé avec les informations de la ligne
     *
     */
    private LigneKbartDto constructDto(String[] line) {
        LigneKbartDto kbartLineInDtoObject = new LigneKbartDto();
        kbartLineInDtoObject.setPublication_title(line[0]);
        kbartLineInDtoObject.setPrint_identifier(line[1]);
        kbartLineInDtoObject.setOnline_identifier(line[2]);
        kbartLineInDtoObject.setDate_first_issue_online(line[3]);
        kbartLineInDtoObject.setNum_first_vol_online(Integer.getInteger(line[4]));
        kbartLineInDtoObject.setNum_first_issue_online(Integer.getInteger(line[5]));
        kbartLineInDtoObject.setDate_last_issue_online(line[6]);
        kbartLineInDtoObject.setNum_last_vol_online(Integer.getInteger(line[7]));
        kbartLineInDtoObject.setNum_last_issue_online(Integer.getInteger(line[8]));
        kbartLineInDtoObject.setTitle_url(line[9]);
        kbartLineInDtoObject.setFirst_author(line[10]);
        kbartLineInDtoObject.setTitle_id(line[11]);
        kbartLineInDtoObject.setEmbargo_info(line[12]);
        kbartLineInDtoObject.setCoverage_depth(line[13]);
        kbartLineInDtoObject.setNotes(line[14]);
        kbartLineInDtoObject.setPublisher_name(line[15]);
        kbartLineInDtoObject.setPublication_type(line[16]);
        kbartLineInDtoObject.setDate_monograph_published_print(line[17]);
        kbartLineInDtoObject.setDate_monograph_published_online(line[18]);
        kbartLineInDtoObject.setMonograph_volume(Integer.getInteger(line[19]));
        kbartLineInDtoObject.setMonograph_edition(line[20]);
        kbartLineInDtoObject.setFirst_editor(line[21]);
        kbartLineInDtoObject.setParent_publication_title_id(line[22]);
        kbartLineInDtoObject.setPreceding_publication_title_id(line[23]);
        kbartLineInDtoObject.setAccess_type(line[23]);
        return kbartLineInDtoObject;
    }

    /**
     * Sérialisation d'un objet dto en chaine de caractère pour le passer au producteur de messages kafka
     * @param dto objet à passer au producteur de messages
     * @return une chaine à passer au TopicProducer de kafka
     */
    private String serializeDTO(LigneKbartDto dto) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(dto);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
