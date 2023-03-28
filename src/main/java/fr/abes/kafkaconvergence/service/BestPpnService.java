package fr.abes.kafkaconvergence.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import fr.abes.kafkaconvergence.dto.LigneKbartDto;
import fr.abes.kafkaconvergence.dto.PpnWithTypeDto;
import fr.abes.kafkaconvergence.dto.ResultWsSudocDto;
import fr.abes.kafkaconvergence.utils.TYPE_SUPPORT;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BestPpnService {
    private final WsService service;

    private Map<String, Long> ppnElecList;

    @Value("${score.online.id.to.ppn}")
    private long scoreOnlineId2Ppn;

    @Value("${score.print.id.to.ppn}")
    private long scorePrintId2Ppn;

    @Value("${score.error.type.notice}")
    private long scoreErrorType;

    public List<String> getBestPpn(LigneKbartDto kbart, String provider) throws JsonProcessingException {
        if (!kbart.getOnline_identifier().isEmpty() && !kbart.getPublication_type().isEmpty()) {
            feedPpnListFromOnline(kbart.getOnline_identifier(), kbart.getPublication_type(), provider);
            feedPpnListFromPrint(kbart.getPrint_identifier(), kbart.getPublication_type());
            if (ppnElecList.isEmpty() && !kbart.getDate_monograph_published_online().isEmpty() && !kbart.getPublication_title().isEmpty()) {
                feedPpnListFromDat(kbart.getDate_monograph_published_online(), kbart.getPublication_title(), kbart.getFirst_author(), kbart.getFirst_editor());
            }
        }
        return new ArrayList<>();
    }

    public void feedPpnListFromOnline(String onlineIdentifier, String publicationType, String provider) throws JsonProcessingException {
        ResultWsSudocDto result = service.callOnlineId2Ppn(publicationType, onlineIdentifier, provider);
        if (!result.getPpns().isEmpty()) {
            long nbPpnElec = result.getPpns().stream().filter(ppnWithTypeDto -> ppnWithTypeDto.getType().equals(TYPE_SUPPORT.ELECTRONIQUE)).count();
            for (PpnWithTypeDto ppn : result.getPpns()) {
                if (ppn.getType().equals(TYPE_SUPPORT.ELECTRONIQUE)) {
                    ppnElecList.put(ppn.getPpn(), scoreOnlineId2Ppn /nbPpnElec);
                } else if (ppn.getType().equals(TYPE_SUPPORT.IMPRIME)) {
                    //  TODO mettre de coté le ppn
                }
            }
        }
    }

    public void feedPpnListFromPrint(String printIdentifier, String publicationType) throws JsonProcessingException {
        ResultWsSudocDto result = service.callPrintId2Ppn(publicationType, printIdentifier);
        if(!result.getPpns().isEmpty()) {
            long nbPpnElec = result.getPpns().stream().filter(ppnWithTypeDto -> ppnWithTypeDto.getType().equals(TYPE_SUPPORT.ELECTRONIQUE)).count();
            long nbPpnPrint = result.getPpns().stream().filter(ppnWithTypeDto -> ppnWithTypeDto.getType().equals(TYPE_SUPPORT.IMPRIME)).count();
            if(nbPpnPrint > 0){
                for (PpnWithTypeDto ppn : result.getPpns()) {
                    if (ppn.getType().equals(TYPE_SUPPORT.ELECTRONIQUE)) {
                        ppnElecList.put(ppn.getPpn(), scorePrintId2Ppn /nbPpnPrint);
                    } else if (ppn.getType().equals(TYPE_SUPPORT.IMPRIME)) {
                        //  TODO mettre de coté le ppn
                    }
                }
            }
        }
    }

    public void feedPpnListFromDat(String monographPublishedOnline, String publicationTitle, @Nullable String firstAuthor, @Nullable String firstEditor) {
        //  TODO compléter le code
    }

//    private long getNbPpnElecFromList(List<PpnWithTypeDto> listPpnWithType) {
//        return listPpnWithType.stream().filter(ppnWithTypeDto -> ppnWithTypeDto.getType().equals(TYPE_SUPPORT.ELECTRONIQUE)).count();
//    }
}
