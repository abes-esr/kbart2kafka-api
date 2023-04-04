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

    private Map<String, Long> ppnList = new HashMap<>();

    public void addPpn(String ppn, Long value) {
        this.ppnList.put(ppn, value);
    }

    public void addPpnList(Map<String, Long> ppnList) {
        ppnList.entrySet().forEach(entry -> {
            this.ppnList.put(entry.getKey(), entry.getValue());
        });

    }
}
