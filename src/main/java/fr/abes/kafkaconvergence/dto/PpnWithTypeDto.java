package fr.abes.kafkaconvergence.dto;

import fr.abes.kafkaconvergence.utils.TYPE_SUPPORT;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PpnWithTypeDto {
    private String ppn;
    private TYPE_SUPPORT type;

    private Boolean providerInNoticeIsPresent;

    public PpnWithTypeDto(String ppn) {
        this.ppn = ppn;
    }

    public PpnWithTypeDto(String ppn, TYPE_SUPPORT type) {
        this.ppn = ppn;
        this.type = type;
    }
}
