package fr.abes.kafkaconvergence.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class ErreurResultDto {

    private String serviceName;

    private List<String> ppns = new ArrayList<>();

    private LigneKbartDto ligneKbartDto;

    private List<String> messages = new ArrayList<>();

    public void addPpns(String ppn){
        ppns.add(ppn);
    }

    public void addErreurMessage(String message){
        messages.add(message);
    }
}
