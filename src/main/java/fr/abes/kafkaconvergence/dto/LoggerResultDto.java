package fr.abes.kafkaconvergence.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class LoggerResultDto {
    private String serviceName;

    private List<String> ppns;

    private LigneKbartDto ligneKbartDto;

    private List<String> messages;

    public LoggerResultDto(LigneKbartDto ligneKbartDto) {
        this.ligneKbartDto = ligneKbartDto;
    }

    public LoggerResultDto(String serviceName, LigneKbartDto ligneKbartDto, List<String> messages) {
        this.serviceName = serviceName;
        this.ppns = new ArrayList<>();
        this.ligneKbartDto = ligneKbartDto;
        this.messages = messages;
    }

    public LoggerResultDto(String serviceName, List<String> ppns, LigneKbartDto ligneKbartDto, List<String> messages) {
        this.serviceName = serviceName;
        this.ppns = ppns;
        this.ligneKbartDto = ligneKbartDto;
        this.messages = messages;
    }

    public void addPpn(String ppn){
        ppns.add(ppn);
    }

    public void addErreurMessage(String message){
        messages.add(message);
    }
}
