package fr.abes.kafkaconvergence.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import fr.abes.kafkaconvergence.dto.LigneKbartDto;
import fr.abes.kafkaconvergence.dto.PpnWithTypeDto;
import fr.abes.kafkaconvergence.dto.ResultDat2PpnWebDto;
import fr.abes.kafkaconvergence.dto.ResultWsSudocDto;
import fr.abes.kafkaconvergence.entity.PpnResultList;
import fr.abes.kafkaconvergence.entity.basexml.notice.NoticeXml;
import fr.abes.kafkaconvergence.exception.IllegalPpnException;
import fr.abes.kafkaconvergence.exception.ScoreException;
import fr.abes.kafkaconvergence.utils.TYPE_SUPPORT;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Getter
@Slf4j
public class BestPpnService {
    private final WsService service;

    private PpnResultList ppnResultList;


    @Value("${score.online.id.to.ppn}")
    private long scoreOnlineId2Ppn;

    @Value("${score.print.id.to.ppn}")
    private long scorePrintId2Ppn;

    @Value("${score.error.type.notice}")
    private long scoreErrorType;

    @Value("${score.dat.to.ppn}")
    private long scoreDat2Ppn;

    private final NoticeService noticeService;

    public BestPpnService(WsService service, NoticeService noticeService) {
        this.service = service;
        this.noticeService = noticeService;
        this.ppnResultList = new PpnResultList();
    }

//    public List<String> sortByBestPpn(Map<String, Long> list) {
//        List<String> result = new ArrayList<>();
//        result.add(Collections.max(list.entrySet(), Map.Entry.comparingByValue()).getKey());
//        return result;
//    }

    public PpnResultList getBestPpn(LigneKbartDto kbart, String provider) throws IOException, IllegalPpnException {
        this.ppnResultList = new PpnResultList();
        if (!kbart.getOnline_identifier().isEmpty() && !kbart.getPublication_type().isEmpty()) {
            feedPpnListFromOnline(kbart, provider);
            feedPpnListFromPrint(kbart, provider);
            if (ppnResultList.getMapPpnScore().isEmpty()) {
                feedPpnListFromDat(kbart);
            }
        }
        if (ppnResultList.getMapPpnScore().isEmpty()) {
            log.error("BestPpn " + kbart.toString() + " Aucun bestPpn trouvé.");
        }
        return ppnResultList;
    }

    public void feedPpnListFromOnline(LigneKbartDto kbart, String provider) throws JsonProcessingException {
        ResultWsSudocDto resultCallWs = service.callOnlineId2Ppn(kbart.getPublication_type(), kbart.getOnline_identifier(), provider);
        getResultFromCall(kbart, resultCallWs, scoreOnlineId2Ppn, "OnlineId2Ppn ");
    }

    public void feedPpnListFromPrint(LigneKbartDto kbart, String provider) throws JsonProcessingException {
        ResultWsSudocDto resultCallWs = service.callPrintId2Ppn(kbart.getPublication_type(), kbart.getPrint_identifier(), provider);
        getResultFromCall(kbart, resultCallWs, scorePrintId2Ppn, "PrintId2Ppn ");
    }

    private void getResultFromCall(LigneKbartDto kbart, ResultWsSudocDto resultCallWs, long scoreOnlineId2Ppn, String s) {
        if (!resultCallWs.getPpns().isEmpty()) {
            long nbPpnElec = resultCallWs.getPpns().stream().filter(ppnWithTypeDto -> ppnWithTypeDto.getType().equals(TYPE_SUPPORT.ELECTRONIQUE)).count();
            for (PpnWithTypeDto ppn : resultCallWs.getPpns()) {
                if (ppn.getType().equals(TYPE_SUPPORT.ELECTRONIQUE)) {
                    this.ppnResultList.addPpnWithType(ppn.getPpn(), ppn.getType(), scoreOnlineId2Ppn / nbPpnElec);
                } else if (ppn.getType().equals(TYPE_SUPPORT.IMPRIME)) {
                    this.ppnResultList.addPpnWithType(ppn.getPpn(), ppn.getType(), 0L);
                }
            }
            Stream<PpnWithTypeDto> ppnImprimes = this.ppnResultList.getMapPpnScore().keySet().stream().filter(ppn -> ppn.getType().equals(TYPE_SUPPORT.IMPRIME));
            if (ppnImprimes.count() > 1) {
                log.error(s + kbart.toString() + " Plus d'un ppn de type imprimé a été trouvé : " + ppnImprimes.map(PpnWithTypeDto::getPpn).collect(Collectors.joining(";")));
            }
            if (nbPpnElec > 1) {
                log.error(s + kbart.toString() + " " + resultCallWs.getErreurs().stream().map(String::toString).collect(Collectors.joining(", ")));
            }
        }
    }

    public void feedPpnListFromDat(LigneKbartDto kbart) throws IOException, IllegalPpnException {
        if (!kbart.getDate_monograph_published_online().isEmpty()) {
            ResultDat2PpnWebDto resultDat2PpnWeb = service.callDat2Ppn(kbart.getDate_monograph_published_online(), kbart.getAuthor(), kbart.getPublication_title());
            for (String ppn : resultDat2PpnWeb.getPpns()) {
                NoticeXml notice = noticeService.getNoticeByPpn(ppn);
                if (notice.isNoticeElectronique()) {
                    this.ppnResultList.addPpnWithType(ppn, TYPE_SUPPORT.ELECTRONIQUE, scoreDat2Ppn);
                }
            }
        }
        if (this.ppnResultList.getMapPpnScore().isEmpty() && !kbart.getDate_monograph_published_print().isEmpty()) {
            ResultDat2PpnWebDto resultDat2PpnWeb = service.callDat2Ppn(kbart.getDate_monograph_published_print(), kbart.getAuthor(), kbart.getPublication_title());
            for (String ppn : resultDat2PpnWeb.getPpns()) {
                NoticeXml notice = noticeService.getNoticeByPpn(ppn);
                if (notice.isNoticeImprimee()) {
                    this.ppnResultList.addPpnWithType(ppn, TYPE_SUPPORT.IMPRIME, 0L);
                }
            }

            Stream<PpnWithTypeDto> ppnImprimes = this.ppnResultList.getMapPpnScore().keySet().stream().filter(ppn -> ppn.getType().equals(TYPE_SUPPORT.IMPRIME));
            if (ppnImprimes.count() > 1) {
                log.error("Dat2Ppn " + kbart.toString() + "Plus d'un ppn de type imprimé a été trouvé : " + ppnImprimes.map(PpnWithTypeDto::getPpn).collect(Collectors.joining(";")));
            }
        }
    }

    public List<String> getBestPpnByScore(PpnResultList ppns) throws ScoreException {
        List<String> result = new ArrayList<>();
        //cas d'un seul ppn électronique
        List<PpnWithTypeDto> ppnElect = ppns.getMapPpnScore().keySet().stream().filter(ppn -> ppn.getType().equals(TYPE_SUPPORT.ELECTRONIQUE)).collect(Collectors.toList());
        if (ppnElect.size() == 1) {
            result.add(ppnElect.get(0).getPpn());
            return result;
        }

        //cas de plusieurs ppn avec une pondération identique
        List<PpnWithTypeDto> ppnElectMemeScore = new ArrayList<>();
        ppnElectMemeScore.add(Collections.max(ppns.getMapPpnScore().entrySet(), Map.Entry.comparingByValue()).getKey());
        if (ppnElectMemeScore.size() > 1) {
            log.error("Les ppn " + ppnElectMemeScore.stream().map(PpnWithTypeDto::getPpn).collect(Collectors.joining(", ")) + " ont le même score");
            throw new ScoreException("Les ppn " + ppnElectMemeScore.stream().map(PpnWithTypeDto::getPpn).collect(Collectors.joining(", ")) + " ont le même score");
        }

        //cas ppn imprimé
        result.addAll(ppns.getMapPpnScore().keySet().stream().filter(ppn -> ppn.getType().equals(TYPE_SUPPORT.IMPRIME)).map(PpnWithTypeDto::getPpn).collect(Collectors.toList()));
        return result;
    }
}
