package fr.abes.kafkaconvergence.entity;

import fr.abes.kafkaconvergence.dto.PpnWithTypeDto;
import fr.abes.kafkaconvergence.utils.TYPE_SUPPORT;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class PpnResultList {
    private Map<PpnWithTypeDto, Long> mapPpnScore = new HashMap<>();

    public void addPpn(String ppn, Long value) {
        this.mapPpnScore.put(new PpnWithTypeDto(ppn), value);
    }

    public void addPpnWithType(String ppn, TYPE_SUPPORT type, Long value) {
        this.mapPpnScore.put(new PpnWithTypeDto(ppn, type), value);
    }
    public void addPpnList(Map<PpnWithTypeDto, Long> ppnList) {
        this.mapPpnScore.putAll(ppnList);

    }
}
