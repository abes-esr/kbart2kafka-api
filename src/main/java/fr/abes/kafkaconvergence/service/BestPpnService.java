package fr.abes.kafkaconvergence.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import fr.abes.kafkaconvergence.dto.LigneKbartDto;
import fr.abes.kafkaconvergence.dto.PpnWithTypeDto;
import fr.abes.kafkaconvergence.dto.ResultWsSudocDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BestPpnService {
    private final WsService service;

    public List<String> getBestPpn(LigneKbartDto kbart, String provider) throws JsonProcessingException {
        ResultWsSudocDto result = service.callOnlineId2Ppn(kbart.getPublication_type(), kbart.getOnline_identifier(), provider);
        return result.getPpns().stream().map(PpnWithTypeDto::getPpn).toList();
    }
}
