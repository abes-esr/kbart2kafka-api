package fr.abes.kafkaconvergence.dto;

import fr.abes.kafkaconvergence.utils.TYPE_SUPPORT;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PpnWithTypeDto {
    String ppn;
    TYPE_SUPPORT type;
}
