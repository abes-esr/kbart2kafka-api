package fr.abes.kafkaconvergence.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import fr.abes.kafkaconvergence.dto.LigneKbartDto;
import fr.abes.kafkaconvergence.dto.PpnWithTypeDto;
import fr.abes.kafkaconvergence.dto.ResultDat2PpnWebDto;
import fr.abes.kafkaconvergence.dto.ResultWsSudocDto;
import fr.abes.kafkaconvergence.entity.basexml.notice.NoticeXml;
import fr.abes.kafkaconvergence.exception.BestPpnException;
import fr.abes.kafkaconvergence.exception.IllegalPpnException;
import fr.abes.kafkaconvergence.utils.TYPE_SUPPORT;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
@Getter
@Slf4j
public class BestPpnService {
    private final WsService service;

    private Map<String, Integer> ppnElecResultList;

    private List<String> ppnPrintResultList;

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

    private final TopicProducer topicProducer;

    public BestPpnService(WsService service, NoticeService noticeService, TopicProducer topicProducer) {
        this.service = service;
        this.noticeService = noticeService;
        this.topicProducer = topicProducer;
        this.ppnElecResultList = new HashMap<>();
        this.ppnPrintResultList = new ArrayList<>();
    }

    public String getBestPpn(LigneKbartDto kbart, String provider) throws IOException, IllegalPpnException, BestPpnException {
        if (!kbart.getPublication_type().isEmpty()) {
            if (!kbart.getOnline_identifier().isEmpty()) {
                feedPpnListFromOnline(kbart, provider);
            }
            if (!kbart.getPrint_identifier().isEmpty()) {
                feedPpnListFromPrint(kbart, provider);
            }
        }
        if (ppnElecResultList.isEmpty()) {
            feedPpnListFromDat(kbart);
        }
        if (ppnElecResultList.isEmpty()) {
            log.error("BestPpn " + kbart.toString() + " Aucun bestPpn trouvé.");
        }

        return getBestPpnByScore(kbart, provider);
    }

    public void feedPpnListFromOnline(LigneKbartDto kbart, String provider) throws JsonProcessingException {
        getResultFromCall(service.callOnlineId2Ppn(kbart.getPublication_type(), kbart.getOnline_identifier(), provider));
    }

    public void feedPpnListFromPrint(LigneKbartDto kbart, String provider) throws JsonProcessingException {
        getResultFromCall(service.callPrintId2Ppn(kbart.getPublication_type(), kbart.getPrint_identifier(), provider));
    }

    private void getResultFromCall(ResultWsSudocDto resultCallWs) {
        if (!resultCallWs.getPpns().isEmpty()) {
            int nbPpnElec = (int) resultCallWs.getPpns().stream().filter(ppnWithTypeDto -> ppnWithTypeDto.getType().equals(TYPE_SUPPORT.ELECTRONIQUE)).count();
            for (PpnWithTypeDto ppn : resultCallWs.getPpns()) {
                if (ppn.getType().equals(TYPE_SUPPORT.ELECTRONIQUE)) {
                    if (!this.ppnElecResultList.isEmpty()) {
                        if (this.ppnElecResultList.containsKey(ppn.getPpn())) {
                            Integer value = this.ppnElecResultList.get(ppn.getPpn()) + (this.scoreOnlineId2PpnElect / nbPpnElec);
                            this.ppnElecResultList.put(ppn.getPpn(), value);
                        } else {
                            this.ppnElecResultList.put(ppn.getPpn(), (this.scoreOnlineId2PpnElect / nbPpnElec));
                        }
                    } else {
                        this.ppnElecResultList.put(ppn.getPpn(), (this.scoreOnlineId2PpnElect / nbPpnElec));
                    }
                } else if (ppn.getType().equals(TYPE_SUPPORT.IMPRIME)) {
                    this.ppnPrintResultList.add(ppn.getPpn());
                }
            }
        }
    }

    public void feedPpnListFromDat(LigneKbartDto kbart) throws IOException, IllegalPpnException {
        if (!kbart.getDate_monograph_published_online().isEmpty()) {
            ResultDat2PpnWebDto resultDat2PpnWeb = service.callDat2Ppn(kbart.getDate_monograph_published_online(), kbart.getAuthor(), kbart.getPublication_title());
            for (String ppn : resultDat2PpnWeb.getPpns()) {
                NoticeXml notice = noticeService.getNoticeByPpn(ppn);
                if (notice.isNoticeElectronique()) {
                    this.ppnElecResultList.put(ppn, scoreDat2Ppn);
                }
            }
        }
        if (this.ppnElecResultList.isEmpty() && !kbart.getDate_monograph_published_print().isEmpty()) {
            ResultDat2PpnWebDto resultDat2PpnWeb = service.callDat2Ppn(kbart.getDate_monograph_published_print(), kbart.getAuthor(), kbart.getPublication_title());
            for (String ppn : resultDat2PpnWeb.getPpns()) {
                NoticeXml notice = noticeService.getNoticeByPpn(ppn);
                if (notice.isNoticeImprimee()) {
                    this.ppnPrintResultList.add(ppn);
                }
            }
        }
    }

    public String getBestPpnByScore(LigneKbartDto kbart, String provider) throws BestPpnException, JsonProcessingException {
        Map<String, Integer> ppnElecScore = getMaxValuesFromMap(ppnElecResultList);
        switch (ppnElecScore.size()) {
            case 0 :
                switch (ppnPrintResultList.size()) {
                    case 0 :
                        topicProducer.sendPrintNotice(null, kbart, provider);
                        //  TODO alimenter topic "création d'une e-notice à partir d'une ligne kbart + info provider"
                        break;
                    case 1 :
                        topicProducer.sendPrintNotice(ppnPrintResultList.get(0), kbart, provider);
                        // TODO alimenter topic "création de la e-notice à partir de la notice imprimée + kbart + infos provider"
                        break;
                    default :
                        throw new BestPpnException("Plusieurs ppn imprimés (" + String.join(", ", ppnElecScore.keySet()) + ") ont été trouvés.");
                }
                break;
            case 1 :
                return ppnElecScore.keySet().stream().findFirst().get();
            default :
                throw new BestPpnException("Les ppn électroniques " + String.join(", ", ppnElecScore.keySet()) + " ont le même score");

        }
        return "";
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
