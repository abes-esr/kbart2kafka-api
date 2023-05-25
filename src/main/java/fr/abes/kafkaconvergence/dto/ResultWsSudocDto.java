package fr.abes.kafkaconvergence.dto;

import fr.abes.kafkaconvergence.utils.TYPE_SUPPORT;
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

    public ResultWsSudocDto getPpnWithTypeElectronique() {
        ResultWsSudocDto result = new ResultWsSudocDto();
        List<PpnWithTypeDto> ppnsSorted = new ArrayList<>(this.ppns.stream().filter(ppnWithTypeDto -> ppnWithTypeDto.getType().equals(TYPE_SUPPORT.ELECTRONIQUE)).toList());
        result.setPpns(ppnsSorted);
        result.setErreurs(this.erreurs);
        return result;
    }

    public ResultWsSudocDto getPpnWithTypeImprime() {
        ResultWsSudocDto result = new ResultWsSudocDto();
        List<PpnWithTypeDto> ppnsSorted = new ArrayList<>(this.ppns.stream().filter(ppnWithTypeDto -> ppnWithTypeDto.getType().equals(TYPE_SUPPORT.IMPRIME)).toList());
        result.setPpns(ppnsSorted);
        result.setErreurs(this.erreurs);
        return result;
    }
}
