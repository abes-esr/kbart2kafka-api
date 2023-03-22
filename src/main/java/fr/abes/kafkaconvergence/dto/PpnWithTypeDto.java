package fr.abes.kafkaconvergence.dto;

import fr.abes.kafkaconvergence.utils.TYPE_SUPPORT;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PpnWithTypeDto {
    private String ppn;
    private TYPE_SUPPORT type;
}
