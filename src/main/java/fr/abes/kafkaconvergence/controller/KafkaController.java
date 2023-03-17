package fr.abes.kafkaconvergence.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.abes.kafkaconvergence.dto.LigneKbartWebDto;
import fr.abes.kafkaconvergence.service.TopicProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @PostMapping("/kbart2Kafka")
    public void kbart2kafka(MultipartFile file) throws IOException {
        //execution seulement si:
        //le fichier à une extension tsv,
        //contient des tabulations,
        //contient un entête avec la présence du terme publication title
        if(isFileWithTSVExtension(file) && detectTabulations(file) && detectOfHeaderPresence("publication_title", file)){

        }
        // controle si presence de tabulations dans les fichiers
        ;
        //lecture fichier, ligne par ligne, creation objet java pour chaque ligne
        List<LigneKbartWebDto> listLigneKbartWebDto = constructLigneKbartWebDtoList(file);
        //injection de chaque ligne dans kafka (cle = id genere ad hoc par appli, valeur = objet java)
        for(LigneKbartWebDto ligne : listLigneKbartWebDto){
            topicProducer.send(String.valueOf(ligne.hashCode()),serializeDTO(ligne));
        }
    }

    /**
     * Controle si le fichier à bien une extension tsv
     * @param file fichier en entrée
     * @return true si extension présente, false sinon
     * @throws IOException erreur avec le fichier en entrée
     */
    private boolean isFileWithTSVExtension(MultipartFile file) throws IOException {
        //Filename extension control
        String fileName = file.getOriginalFilename(); // get file name
        if (fileName == null || fileName.isEmpty()) return false; // check if file name is valid
        String[] parts = fileName.split("\\."); // split by dot
        String extension = parts[parts.length - 1]; // get last part as extension

        return extension.equalsIgnoreCase("tsv"); // compare with tsv ignoring case
    }

    /**
     * Détecte si le fichier présente des tabulations
     * @param file fichier en entrée
     * @return true si des tabulations sont présentes dans le fichier, false sinon
     * @throws IOException erreur avec le fichier en entrée
     */
    private boolean detectTabulations(MultipartFile file) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("\t")) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Détecte la présence d'une entête dans le fichier
     * @param header terme à recherche dans l'entête
     * @param file fichier en entrée
     * @return true si le terme est présent
     * @throws IOException
     */
    private boolean detectOfHeaderPresence(String header, MultipartFile file) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.contains(header)) {
                   return true;
                }
            }
        }
        return false;
    }

    /**
     * Construction des objets dto placés dans une liste
     * @param file fichier en entrée
     * @return Une liste d'objets DTO, ouchaque objet dto représente une ligne du fichier kbart en entrée
     * @throws IOException
     */
    private List<LigneKbartWebDto> constructLigneKbartWebDtoList(MultipartFile file) throws IOException {
        List<LigneKbartWebDto> ligneKbartWebDtoList = new LinkedList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.contains("publication_title")) {
                    String[] tsvElementsOnOneLine = line.split("\t");
                    // Crée un nouvel objet dto et set les différentes parties
                    LigneKbartWebDto kbartLineInDtoObject = new LigneKbartWebDto();
                    kbartLineInDtoObject.setPublication_title(tsvElementsOnOneLine[0]);
                    kbartLineInDtoObject.setPrint_identifier(tsvElementsOnOneLine[1]);
                    kbartLineInDtoObject.setOnline_identifier(tsvElementsOnOneLine[2]);
                    kbartLineInDtoObject.setDate_first_issue_online(tsvElementsOnOneLine[3]);
                    kbartLineInDtoObject.setNum_first_vol_online(Integer.getInteger(tsvElementsOnOneLine[4]));
                    kbartLineInDtoObject.setNum_first_issue_online(Integer.getInteger(tsvElementsOnOneLine[5]));
                    kbartLineInDtoObject.setDate_last_issue_online(tsvElementsOnOneLine[6]);
                    kbartLineInDtoObject.setNum_last_vol_online(Integer.getInteger(tsvElementsOnOneLine[7]));
                    kbartLineInDtoObject.setNum_last_issue_online(Integer.getInteger(tsvElementsOnOneLine[8]));
                    kbartLineInDtoObject.setTitle_url(tsvElementsOnOneLine[9]);
                    kbartLineInDtoObject.setFirst_author(tsvElementsOnOneLine[10]);
                    kbartLineInDtoObject.setTitle_id(tsvElementsOnOneLine[11]);
                    kbartLineInDtoObject.setEmbargo_info(tsvElementsOnOneLine[12]);
                    kbartLineInDtoObject.setCoverage_depth(tsvElementsOnOneLine[13]);
                    kbartLineInDtoObject.setNotes(tsvElementsOnOneLine[14]);
                    kbartLineInDtoObject.setPublisher_name(tsvElementsOnOneLine[15]);
                    kbartLineInDtoObject.setPublication_title(tsvElementsOnOneLine[16]);
                    kbartLineInDtoObject.setDate_monograph_published_print(tsvElementsOnOneLine[17]);
                    kbartLineInDtoObject.setDate_monograph_published_online(tsvElementsOnOneLine[18]);
                    kbartLineInDtoObject.setMonograph_volume(Integer.getInteger(tsvElementsOnOneLine[19]));
                    kbartLineInDtoObject.setMonograph_edition(tsvElementsOnOneLine[20]);
                    kbartLineInDtoObject.setFirst_editor(tsvElementsOnOneLine[21]);
                    kbartLineInDtoObject.setParent_publication_title_id(tsvElementsOnOneLine[22]);
                    kbartLineInDtoObject.setPreceding_publication_title_id(tsvElementsOnOneLine[23]);
                    kbartLineInDtoObject.setAccess_type(tsvElementsOnOneLine[23]);
                    //Ajout a la liste de l'objet dto finalisé
                    ligneKbartWebDtoList.add(kbartLineInDtoObject);
                }
            }
        }
        return ligneKbartWebDtoList;
    }

    /**
     * Sérialisation d'un objet dto en chaine de caractère pour le passer au producteur de messages kafka
     * @param dto objet à passer au producteur de messages
     * @return une chaine à passer au TopicProducer de kafka
     */
    private String serializeDTO(LigneKbartWebDto dto) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(dto);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
