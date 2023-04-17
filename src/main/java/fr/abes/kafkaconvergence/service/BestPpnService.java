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
    @Value("${score.online.id.to.ppn.elect}")
    private int scoreOnlineId2PpnElect;

    @Value("${score.print.id.to.ppn.elect}")
    private int scorePrintId2PpnElect;

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
    }

    public String getBestPpn(LigneKbartDto kbart, String provider) throws IOException, IllegalPpnException, BestPpnException {

        Map<String, Integer> ppnElecResultList = new HashMap<>();
        List<String> ppnPrintResultList = new ArrayList<>();

        if (!kbart.getPublication_type().isEmpty()) {
            if (!kbart.getOnline_identifier().isEmpty()) {
                log.debug("paramètres en entrée : type : " + kbart.getPublication_type() + " / id : " + kbart.getOnline_identifier() + " / provider : " + provider);
                feedPpnListFromOnline(kbart, provider, ppnElecResultList, ppnPrintResultList);
            }
            if (!kbart.getPrint_identifier().isEmpty()) {
                log.debug("paramètres en entrée : type : " + kbart.getPublication_type() + " / id : " + kbart.getOnline_identifier() + " / provider : " + provider);
                feedPpnListFromPrint(kbart, provider, ppnElecResultList, ppnPrintResultList);
            }
        }
        if (ppnElecResultList.isEmpty()) {
            feedPpnListFromDat(kbart, ppnElecResultList, ppnPrintResultList);
        }
        if (ppnElecResultList.isEmpty()) {
            log.error("BestPpn " + kbart.toString() + " Aucun bestPpn trouvé.");
        }

        return getBestPpnByScore(kbart, provider, ppnElecResultList, ppnPrintResultList);
    }

    public void feedPpnListFromOnline(LigneKbartDto kbart, String provider, Map<String, Integer> ppnElecResultList, List<String> ppnPrintResultList) throws JsonProcessingException {
        log.debug("Entrée dans onlineId2Ppn");
        getResultFromCall(service.callOnlineId2Ppn(kbart.getPublication_type(), kbart.getOnline_identifier(), provider), this.scoreOnlineId2PpnElect, ppnElecResultList, ppnPrintResultList);
    }

    public void feedPpnListFromPrint(LigneKbartDto kbart, String provider, Map<String, Integer> ppnElecResultList, List<String> ppnPrintResultList) throws JsonProcessingException {
        log.debug("Entrée dans printId2Ppn");
        getResultFromCall(service.callPrintId2Ppn(kbart.getPublication_type(), kbart.getPrint_identifier(), provider), this.scorePrintId2PpnElect, ppnElecResultList, ppnPrintResultList);
    }

    private void getResultFromCall(ResultWsSudocDto resultCallWs, int score, Map<String, Integer> ppnElecResultList, List<String> ppnPrintResultList) {
        if (!resultCallWs.getPpns().isEmpty()) {
            int nbPpnElec = (int) resultCallWs.getPpns().stream().filter(ppnWithTypeDto -> ppnWithTypeDto.getType().equals(TYPE_SUPPORT.ELECTRONIQUE)).count();
            for (PpnWithTypeDto ppn : resultCallWs.getPpns()) {
                if (ppn.getType().equals(TYPE_SUPPORT.ELECTRONIQUE)) {
                    if (!ppnElecResultList.isEmpty()) {
                        if (ppnElecResultList.containsKey(ppn.getPpn())) {
                            Integer value = ppnElecResultList.get(ppn.getPpn()) + (score / nbPpnElec);
                            log.debug("PPN Electronique : " + ppn + " / score : " + value);
                            ppnElecResultList.put(ppn.getPpn(), value);
                        } else {
                            log.debug("PPN Electronique : " + ppn + " / score : " + score / nbPpnElec);
                            ppnElecResultList.put(ppn.getPpn(), (score / nbPpnElec));
                        }
                    } else {
                        log.debug("PPN Electronique : " + ppn + " / score : " + score / nbPpnElec);
                        ppnElecResultList.put(ppn.getPpn(), (score / nbPpnElec));
                    }
                } else if (ppn.getType().equals(TYPE_SUPPORT.IMPRIME)) {
                    log.debug("PPN Imprimé : " + ppn);
                    ppnPrintResultList.add(ppn.getPpn());
                }
            }
        }
    }

    public void feedPpnListFromDat(LigneKbartDto kbart, Map<String, Integer> ppnElecResultList, List<String> ppnPrintResultList) throws IOException, IllegalPpnException {
        log.debug("Entrée dans dat2ppn");
        if (!kbart.getDate_monograph_published_online().isEmpty()) {
            log.debug("Appel avec date monograph published online : " + kbart.getDate_monograph_published_online());
            ResultDat2PpnWebDto resultDat2PpnWeb = service.callDat2Ppn(kbart.getDate_monograph_published_online(), kbart.getAuthor(), kbart.getPublication_title());
            for (String ppn : resultDat2PpnWeb.getPpns()) {
                NoticeXml notice = noticeService.getNoticeByPpn(ppn);
                if (notice.isNoticeElectronique()) {
                    ppnElecResultList.put(ppn, scoreDat2Ppn);
                } else if (notice.isNoticeImprimee()) {
                    ppnPrintResultList.add(ppn);
                }
            }
        }
        if (ppnElecResultList.isEmpty() && !kbart.getDate_monograph_published_print().isEmpty()) {
            log.debug("Appel avec date monograph published print : " + kbart.getDate_monograph_published_print());
            ResultDat2PpnWebDto resultDat2PpnWeb = service.callDat2Ppn(kbart.getDate_monograph_published_print(), kbart.getAuthor(), kbart.getPublication_title());
            for (String ppn : resultDat2PpnWeb.getPpns()) {
                NoticeXml notice = noticeService.getNoticeByPpn(ppn);
                if (notice.isNoticeImprimee()) {
                    ppnPrintResultList.add(ppn);
                } else if (notice.isNoticeElectronique()) {
                    ppnElecResultList.put(ppn, scoreDat2Ppn);
                }
            }
        }
    }

    public String getBestPpnByScore(LigneKbartDto kbart, String provider, Map<String, Integer> ppnElecResultList, List<String> ppnPrintResultList) throws BestPpnException, JsonProcessingException {
        Map<String, Integer> ppnElecScore = getMaxValuesFromMap(ppnElecResultList);
        switch (ppnElecScore.size()) {
            case 0:
                switch (ppnPrintResultList.size()) {
                    case 0 -> topicProducer.sendPrintNotice(null, kbart, provider);
                    case 1 -> topicProducer.sendPrintNotice(ppnPrintResultList.get(0), kbart, provider);
                    default ->
                            throw new BestPpnException("Plusieurs ppn imprimés (" + String.join(", ", ppnPrintResultList) + ") ont été trouvés.");
                }
                break;
            case 1:
                return ppnElecScore.keySet().stream().findFirst().get();
            default:
                throw new BestPpnException("Les ppn électroniques " + String.join(", ", ppnElecScore.keySet()) + " ont le même score");
        }
        return "";
    }

    public <K, V extends Comparable<? super V>> Map<K, V> getMaxValuesFromMap(Map<K, V> map) {
        Map<K, V> maxKeys = new HashMap<>();
        if (!map.isEmpty()) {
            V maxValue = Collections.max(map.values());
            for (Map.Entry<K, V> entry : map.entrySet()) {
                if (entry.getValue().equals(maxValue)) {
                    maxKeys.put(entry.getKey(), entry.getValue());
                }
            }
        }
        return maxKeys;
    }
}
