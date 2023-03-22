package fr.abes.kafkaconvergence.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ResultWsSudocDto {
    private List<PpnWithTypeDto> ppns = new ArrayList<>();
    private List<String> erreurs = new ArrayList<>();
}
