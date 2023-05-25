package fr.abes.kafkaconvergence.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import fr.abes.kafkaconvergence.dto.*;
import fr.abes.kafkaconvergence.entity.basexml.notice.NoticeXml;
import fr.abes.kafkaconvergence.exception.BestPpnException;
import fr.abes.kafkaconvergence.exception.IllegalPpnException;
import fr.abes.kafkaconvergence.utils.PUBLICATION_TYPE;
import fr.abes.kafkaconvergence.utils.TYPE_SUPPORT;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.regex.Pattern;

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

    @Value("${doi.pattern.url.raw}")
    private String doiPattern;

    private final NoticeService noticeService;

    private final TopicProducer topicProducer;

    private final CheckUrlService checkUrlService;

    public BestPpnService(WsService service, NoticeService noticeService, TopicProducer topicProducer, CheckUrlService checkUrlService) {
        this.service = service;
        this.noticeService = noticeService;
        this.topicProducer = topicProducer;
        this.checkUrlService = checkUrlService;
    }

    public String getBestPpn(LigneKbartDto kbart, String provider) throws IOException, IllegalPpnException, BestPpnException, URISyntaxException {

        Map<String, Integer> ppnElecResultList = new HashMap<>();
        Set<String> ppnPrintResultList = new HashSet<>();

        if (!kbart.getPublication_type().isEmpty()) {
            provider = kbart.getPublication_type().equals(PUBLICATION_TYPE.serial.toString()) ? "" : provider;
            if (!kbart.getOnline_identifier().isEmpty()) {
                log.debug("paramètres en entrée : type : " + kbart.getPublication_type() + " / id : " + kbart.getOnline_identifier() + " / provider : " + provider);
                feedPpnListFromOnline(kbart, provider, ppnElecResultList, ppnPrintResultList);
            }
            if (!kbart.getPrint_identifier().isEmpty()) {
                log.debug("paramètres en entrée : type : " + kbart.getPublication_type() + " / id : " + kbart.getPrint_identifier() + " / provider : " + provider);
                feedPpnListFromPrint(kbart, provider, ppnElecResultList, ppnPrintResultList);
            }
        }
        String doi = extractDOI(kbart);
        if (!doi.isBlank()){
            feedPpnListFromDoi(doi, provider, ppnElecResultList, ppnPrintResultList);
        }

        if (ppnElecResultList.isEmpty()) {
            feedPpnListFromDat(kbart, ppnElecResultList, ppnPrintResultList);
        }
        if (ppnElecResultList.isEmpty()) {
            log.error("BestPpn " + kbart + " Aucun bestPpn trouvé.");
        }


        return getBestPpnByScore(kbart, provider, ppnElecResultList, ppnPrintResultList);
    }

    public String extractDOI(LigneKbartDto kbart) {
        if (kbart.getTitle_url() != null && !kbart.getTitle_url().isEmpty()){
            return Pattern.compile(this.doiPattern).matcher(kbart.getTitle_url()).find() ? kbart.getTitle_url().split("doi.org/")[kbart.getTitle_url().split("doi.org/").length - 1] : "";
        }
        if (kbart.getTitle_id() != null && !kbart.getTitle_id().isEmpty()){
            return Pattern.compile(this.doiPattern).matcher(kbart.getTitle_id()).find() ? kbart.getTitle_id().split("doi.org/")[kbart.getTitle_id().split("doi.org/").length - 1] : "";
        }
        return "";
    }

    public void feedPpnListFromOnline(LigneKbartDto kbart, String provider, Map<String, Integer> ppnElecResultList, Set<String> ppnPrintResultList) throws IOException, IllegalPpnException, URISyntaxException {
        log.debug("Entrée dans onlineId2Ppn");
        getResultFromCall(service.callOnlineId2Ppn(kbart.getPublication_type(), kbart.getOnline_identifier(), provider), kbart.getTitle_url(), this.scoreOnlineId2PpnElect, ppnElecResultList, ppnPrintResultList);
    }

    public void feedPpnListFromPrint(LigneKbartDto kbart, String provider, Map<String, Integer> ppnElecResultList, Set<String> ppnPrintResultList) throws IOException, IllegalPpnException, URISyntaxException {
        log.debug("Entrée dans printId2Ppn");
        ResultWsSudocDto resultCallWs = service.callPrintId2Ppn(kbart.getPublication_type(), kbart.getPrint_identifier(), provider);
        ResultWsSudocDto resultWithTypeElectronique = resultCallWs.getPpnWithTypeElectronique();
        if (resultWithTypeElectronique != null) {
            // lunch service
            getResultFromCall(resultWithTypeElectronique, kbart.getTitle_url(), this.scoreErrorType, ppnElecResultList, ppnPrintResultList);
        }
        ResultWsSudocDto resultWithTypeImprime = resultCallWs.getPpnWithTypeImprime();
        if (resultWithTypeImprime != null) {
            // lunch service
            getResultFromCall(resultWithTypeImprime, kbart.getTitle_url(), this.scorePrintId2PpnElect, ppnElecResultList, ppnPrintResultList);
        }
    }

    private void getResultFromCall(ResultWsSudocDto resultCallWs, String titleUrl, int score, Map<String, Integer> ppnElecResultList, Set<String> ppnPrintResultList) throws URISyntaxException, IOException, IllegalPpnException {
        if (!resultCallWs.getPpns().isEmpty()) {
            int nbPpnElec = (int) resultCallWs.getPpns().stream().filter(ppnWithTypeDto -> ppnWithTypeDto.getType().equals(TYPE_SUPPORT.ELECTRONIQUE)).count();
            for (PpnWithTypeDto ppn : resultCallWs.getPpns()) {
                if (ppn.getType().equals(TYPE_SUPPORT.ELECTRONIQUE) && checkUrlService.checkUrlInNotice(ppn.getPpn(), titleUrl)) {
                    if (!ppnElecResultList.isEmpty()) {
                        if (ppnElecResultList.containsKey(ppn.getPpn())) {
                            Integer value = ppnElecResultList.get(ppn.getPpn()) + (score / nbPpnElec);
                            log.info("PPN Electronique : " + ppn + " / score : " + value);
                            ppnElecResultList.put(ppn.getPpn(), value);
                        } else {
                            log.info("PPN Electronique : " + ppn + " / score : " + score / nbPpnElec);
                            ppnElecResultList.put(ppn.getPpn(), (score / nbPpnElec));
                        }
                    } else {
                        log.info("PPN Electronique : " + ppn + " / score : " + score / nbPpnElec);
                        ppnElecResultList.put(ppn.getPpn(), (score / nbPpnElec));
                    }
                } else if (ppn.getType().equals(TYPE_SUPPORT.IMPRIME)) {
                    log.info("PPN Imprimé : " + ppn);
                    ppnPrintResultList.add(ppn.getPpn());
                }
            }
        }
    }

    public void feedPpnListFromDat(LigneKbartDto kbart, Map<String, Integer> ppnElecResultList, Set<String> ppnPrintResultList) throws IOException, IllegalPpnException {
        if (!kbart.getDate_monograph_published_online().isEmpty()) {
            log.debug("Appel dat2ppn :  date_monograph_published_online : " + kbart.getDate_monograph_published_online() + " / publication_title : " + kbart.getPublication_title() + " auteur : " + kbart.getAuthor());
            ResultDat2PpnWebDto resultDat2PpnWeb = service.callDat2Ppn(kbart.getDate_monograph_published_online(), kbart.getAuthor(), kbart.getPublication_title());
            for (String ppn : resultDat2PpnWeb.getPpns()) {
                log.debug("résultat : ppn " + ppn);
                NoticeXml notice = noticeService.getNoticeByPpn(ppn);
                if (notice.isNoticeElectronique()) {
                    ppnElecResultList.put(ppn, scoreDat2Ppn);
                } else if (notice.isNoticeImprimee()) {
                    ppnPrintResultList.add(ppn);
                }
            }
        }
        if (ppnElecResultList.isEmpty() && !kbart.getDate_monograph_published_print().isEmpty()) {
            log.debug("Appel dat2ppn :  date_monograph_published_print : " + kbart.getDate_monograph_published_online() + " / publication_title : " + kbart.getPublication_title() + " auteur : " + kbart.getAuthor());
            ResultDat2PpnWebDto resultDat2PpnWeb = service.callDat2Ppn(kbart.getDate_monograph_published_print(), kbart.getAuthor(), kbart.getPublication_title());
            for (String ppn : resultDat2PpnWeb.getPpns()) {
                log.debug("résultat : ppn " + ppn);
                NoticeXml notice = noticeService.getNoticeByPpn(ppn);
                if (notice.isNoticeImprimee()) {
                    ppnPrintResultList.add(ppn);
                } else if (notice.isNoticeElectronique()) {
                    ppnElecResultList.put(ppn, scoreDat2Ppn);
                }
            }
        }
    }

    public void feedPpnListFromDoi(String doi, String provider, Map<String, Integer> ppnElecResultList, Set<String> ppnPrintResultList) throws IOException, IllegalPpnException {
        ResultWsSudocDto resultDoi2PpnWebDto = service.callDoi2Ppn(doi, provider);
        for (PpnWithTypeDto ppn : resultDoi2PpnWebDto.getPpns()) {
            if (ppn.getType().equals(TYPE_SUPPORT.ELECTRONIQUE)){
                log.debug("résultat : ppn de type electronique : " + ppn);
                ppnElecResultList.put(ppn.getPpn(), 15 / resultDoi2PpnWebDto.getPpns().size());
            }
            if (ppn.getType().equals(TYPE_SUPPORT.IMPRIME)){
                log.debug("résultat : ppn de type electronique : " + ppn);
                ppnPrintResultList.add(ppn.getPpn());
            }
        }
    }

    public String getBestPpnByScore(LigneKbartDto kbart, String provider, Map<String, Integer> ppnElecResultList, Set<String> ppnPrintResultList) throws BestPpnException, JsonProcessingException {
        Map<String, Integer> ppnElecScore = getMaxValuesFromMap(ppnElecResultList);
        switch (ppnElecScore.size()) {
            case 0 -> {
                switch (ppnPrintResultList.size()) {
                    case 0 -> {
                        log.debug("Envoi kbart et provider vers kafka");
                        topicProducer.sendPrintNotice(null, kbart, provider);
                    }
                    case 1 -> {
                        log.debug("envoi ppn imprimé " + ppnPrintResultList.stream().toList().get(0) + ", kbart et provider");
                        topicProducer.sendPrintNotice(ppnPrintResultList.stream().toList().get(0), kbart, provider);
                    }
                    default -> {
                        kbart.setErrorType("Plusieurs ppn imprimés (" + String.join(", ", ppnPrintResultList) + ") ont été trouvés.");
                        throw new BestPpnException("Plusieurs ppn imprimés (" + String.join(", ", ppnPrintResultList) + ") ont été trouvés.");
                    }
                }
            }
            case 1 -> {
                return ppnElecScore.keySet().stream().findFirst().get();
            }
            default -> {
                String listPpn = String.join(", ", ppnElecScore.keySet());
                String errorString = "Les ppn électroniques " + listPpn + " ont le même score";
                kbart.setErrorType(errorString);
                log.error(errorString);
                throw new BestPpnException(errorString);
            }
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
