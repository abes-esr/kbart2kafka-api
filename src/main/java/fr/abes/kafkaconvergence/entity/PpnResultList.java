package fr.abes.kafkaconvergence.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class PpnResultList {
    private Map<String, Long> mapPpnScore = new HashMap<>();

    public void addPpn(String ppn, Long value) {
        this.mapPpnScore.put(ppn, value);
    }

    public void addPpnList(Map<String, Long> ppnList) {
        this.mapPpnScore.putAll(ppnList);

    }
}
