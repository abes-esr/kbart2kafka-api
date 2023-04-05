package fr.abes.kafkaconvergence.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.abes.kafkaconvergence.dto.LigneKbartDto;
import fr.abes.kafkaconvergence.exception.IllegalFileFormatException;
import fr.abes.kafkaconvergence.exception.IllegalPpnException;
import fr.abes.kafkaconvergence.service.BestPpnService;
import fr.abes.kafkaconvergence.service.TopicProducer;
import fr.abes.kafkaconvergence.utils.CheckFiles;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@RestController
@Slf4j
@RequestMapping("/v1")
public class KafkaController {
    private final TopicProducer topicProducer;
    private final BestPpnService service;
    private final ObjectMapper mapper;

    @ApiOperation("Reads a TSV file, calculates the best PPN and sends the answer to Kafka")
    @ApiResponses({
            @ApiResponse(code = HttpServletResponse.SC_BAD_REQUEST, message = "Wrong file format", response = String.class),
            @ApiResponse(code = HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message = "The server is not responding, please try again later", response = String.class)
    })
    @PostMapping("/kbart2Kafka")
    public void kbart2kafka(@RequestParam("file") MultipartFile file) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            CheckFiles.verifyFile(file, "publication_title");
            String provider = CheckFiles.getProviderFromFilename(file);
            //lecture fichier, ligne par ligne, creation objet java pour chaque ligne
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.contains("publication_title")) {
                    String[] tsvElementsOnOneLine = line.split("\t");
                    // Crée un nouvel objet dto et set les différentes parties
                    LigneKbartDto kbart = constructDto(tsvElementsOnOneLine);
                    List<String> bestPpns = service.getBestPpn(kbart, provider);
                    if (bestPpns.size() > 0) {
                        kbart.setBestPpn(bestPpns.get(0));
                        topicProducer.send(Integer.valueOf(kbart.hashCode()).toString(), mapper.writeValueAsString(kbart));
                    }
                }
            }
        } catch (IllegalFileFormatException ex) {
            throw new IllegalArgumentException(ex.getMessage());
        } catch (IllegalPpnException e) {
            // TODO gérer l'erreur ???
            throw new RuntimeException(e.getMessage());
        }
    }


    /**
     * Construction de la dto
     *
     * @param line ligne en entrée
     * @return Un objet DTO initialisé avec les informations de la ligne
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
     *
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
