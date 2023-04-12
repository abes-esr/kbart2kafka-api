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


    @Value("${score.online.id.to.ppn.elect}")
    private int scoreOnlineId2PpnElect;

    @Value("${score.online.id.to.ppn.imprime}")
    private int scoreOnlineId2PpnImrime;

    @Value("${score.print.id.to.ppn.elect}")
    private int scorePrintId2PpnElect;

    @Value("${score.print.id.to.ppn.imprime}")
    private int scorePrintId2PpnImprime;

    @Value("${score.error.type.notice}")
    private int scoreErrorType;

    @Value("${score.dat.to.ppn}")
    private int scoreDat2Ppn;

    private final NoticeService noticeService;

    public BestPpnService(WsService service, NoticeService noticeService) {
        this.service = service;
        this.noticeService = noticeService;
        this.ppnResultList = new PpnResultList();
    }

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
        getResultFromCall(kbart, resultCallWs, scoreOnlineId2PpnElect, scoreOnlineId2PpnImrime, "OnlineId2Ppn ");
    }

    public void feedPpnListFromPrint(LigneKbartDto kbart, String provider) throws JsonProcessingException {
        ResultWsSudocDto resultCallWs = service.callPrintId2Ppn(kbart.getPublication_type(), kbart.getPrint_identifier(), provider);
        getResultFromCall(kbart, resultCallWs, scorePrintId2PpnElect, scorePrintId2PpnImprime, "PrintId2Ppn ");
    }

    private void getResultFromCall(LigneKbartDto kbart, ResultWsSudocDto resultCallWs, int scoreElect, int scoreImprime, String service) {
        if (!resultCallWs.getPpns().isEmpty()) {
            int nbPpnElec = (int) resultCallWs.getPpns().stream().filter(ppnWithTypeDto -> ppnWithTypeDto.getType().equals(TYPE_SUPPORT.ELECTRONIQUE)).count();
            for (PpnWithTypeDto ppn : resultCallWs.getPpns()) {
                if (ppn.getType().equals(TYPE_SUPPORT.ELECTRONIQUE)) {
                    if (!this.ppnResultList.getMapPpnScore().isEmpty()) {
                        PpnWithTypeDto ppnWithTypeDtoTest = new PpnWithTypeDto();
                        ppnWithTypeDtoTest.setPpn(ppn.getPpn());
                        ppnWithTypeDtoTest.setType(TYPE_SUPPORT.ELECTRONIQUE);
                        if (this.ppnResultList.getMapPpnScore().containsKey(ppnWithTypeDtoTest)) {
                            Integer value = this.ppnResultList.getMapPpnScore().get(ppnWithTypeDtoTest) + (scoreElect / nbPpnElec);
                            this.ppnResultList.addPpnWithType(ppn.getPpn(), ppn.getType(), value);
                        } else {
                            this.ppnResultList.addPpnWithType(ppn.getPpn(), ppn.getType(), (scoreElect / nbPpnElec));
                        }
                    } else {
                        this.ppnResultList.addPpnWithType(ppn.getPpn(), ppn.getType(), (scoreElect / nbPpnElec));
                    }
                } else if (ppn.getType().equals(TYPE_SUPPORT.IMPRIME)) {
                    this.ppnResultList.addPpnWithType(ppn.getPpn(), ppn.getType(), scoreImprime);
                }
            }
            Stream<PpnWithTypeDto> ppnImprimes = this.ppnResultList.getMapPpnScore().keySet().stream().filter(ppn -> ppn.getType().equals(TYPE_SUPPORT.IMPRIME));
            if (ppnImprimes.count() > 1) {
                log.error(service + kbart.toString() + " Plus d'un ppn de type imprimé a été trouvé : " + ppnImprimes.map(PpnWithTypeDto::getPpn).collect(Collectors.joining(";")));
            }
            if (nbPpnElec > 1) {
                log.error(service + kbart.toString() + " " + resultCallWs.getErreurs().stream().map(String::toString).collect(Collectors.joining(", ")));
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
                    this.ppnResultList.addPpnWithType(ppn, TYPE_SUPPORT.IMPRIME, 0);
                }
            }

            Stream<PpnWithTypeDto> ppnImprimes = this.ppnResultList.getMapPpnScore().keySet().stream().filter(ppn -> ppn.getType().equals(TYPE_SUPPORT.IMPRIME));
            if (ppnImprimes.count() > 1) {
                log.error("Dat2Ppn " + kbart.toString() + "Plus d'un ppn de type imprimé a été trouvé : " + ppnImprimes.map(PpnWithTypeDto::getPpn).collect(Collectors.joining(";")));
            }
        }
    }

    public String getBestPpnByScore(PpnResultList ppns) throws ScoreException {
        Map<PpnWithTypeDto, Integer> ppnScore = getMaxValuesFromMap(ppns.getMapPpnScore());
        if (ppnScore.size() == 1) {
            return ppnScore.keySet().stream().findFirst().get().getPpn();
        }
        //cas de plusieurs ppn avec une pondération identique

        if (ppnScore.size() > 1) {
            log.error("Les ppn " + ppnScore.keySet().stream().map(PpnWithTypeDto::getPpn).collect(Collectors.joining(", ")) + " ont le même score");
            throw new ScoreException("Les ppn " + ppnScore.keySet().stream().map(PpnWithTypeDto::getPpn).collect(Collectors.joining(", ")) + " ont le même score");
        }

        return "Aucun best ppn";
    }

    public <K,V extends Comparable<? super V>> Map<K, V> getMaxValuesFromMap(Map<K,V> map) {
        Map<K, V> maxKeys = new HashMap<>();
        V maxValue = Collections.max(map.values());
        for (Map.Entry<K,V> entry : map.entrySet()) {
            if (entry.getValue().equals(maxValue)) {
                maxKeys.put(entry.getKey(), entry.getValue());
            }
        }
        return maxKeys;
    }
}
