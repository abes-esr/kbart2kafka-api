package fr.abes.kafkaconvergence.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.abes.kafkaconvergence.dto.LigneKbartDto;
import fr.abes.kafkaconvergence.dto.PpnWithTypeDto;
import fr.abes.kafkaconvergence.dto.ResultWsSudocDto;
import fr.abes.kafkaconvergence.logger.Logger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BestPpnService {
    private final WsService service;

    private final Logger logger;


    public List<String> getBestPpn(LigneKbartDto kbart, String provider) throws JsonProcessingException {
        ResultWsSudocDto result = service.callOnlineId2Ppn(kbart.getPublication_type(), kbart.getOnline_identifier(), provider);

        logger.error(Integer.valueOf(kbart.hashCode()).toString(), result.getErreurs());


        return result.getPpns().stream().map(PpnWithTypeDto::getPpn).toList();
    }
}
