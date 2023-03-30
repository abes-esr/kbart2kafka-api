package fr.abes.kafkaconvergence.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
public class SearchDatWebDto {
    private Integer date;
    private String auteur;
    private String titre;

    public SearchDatWebDto(String titre) {
        this.titre = titre;
    }
}
