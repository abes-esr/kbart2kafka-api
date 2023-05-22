package fr.abes.kafkaconvergence.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ResultDoi2PpnWebDto {
    @JsonProperty("ppns")
    List<PpnWithTypeDto> ppns = new ArrayList<>();
    @JsonProperty("erreurs")
    List<String> erreurs = new ArrayList<>();

    public void addPpn(PpnWithTypeDto ppn) {
        this.ppns.add(ppn);
    }
}
