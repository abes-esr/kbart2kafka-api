package fr.abes.kafkaconvergence.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import fr.abes.kafkaconvergence.dto.LigneKbartDto;
import fr.abes.kafkaconvergence.dto.PpnWithTypeDto;
import fr.abes.kafkaconvergence.dto.ResultWsSudocDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BestPpnService {
    private final WsService service;



    public List<String> getBestPpn(LigneKbartDto kbart, String provider) throws JsonProcessingException {
        ResultWsSudocDto result = service.callOnlineId2Ppn(kbart.getPublication_type(), kbart.getOnline_identifier(), provider);

        log.error("OnlineId2Ppn " + kbart.toString() + " " + result.getErreurs().stream().map(String::toString).collect(Collectors.joining(", ")));

        return result.getPpns().stream().map(PpnWithTypeDto::getPpn).toList();
    }
}
