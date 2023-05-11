package fr.abes.kafkaconvergence.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import fr.abes.kafkaconvergence.dto.LigneKbartDto;
import fr.abes.kafkaconvergence.dto.PpnWithTypeDto;
import fr.abes.kafkaconvergence.dto.ResultDat2PpnWebDto;
import fr.abes.kafkaconvergence.dto.ResultWsSudocDto;
import fr.abes.kafkaconvergence.entity.basexml.notice.Datafield;
import fr.abes.kafkaconvergence.entity.basexml.notice.NoticeXml;
import fr.abes.kafkaconvergence.entity.basexml.notice.SubField;
import fr.abes.kafkaconvergence.exception.BestPpnException;
import fr.abes.kafkaconvergence.exception.IllegalPpnException;
import fr.abes.kafkaconvergence.utils.PUBLICATION_TYPE;
import fr.abes.kafkaconvergence.utils.TYPE_SUPPORT;
import fr.abes.kafkaconvergence.utils.Utils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

    public BestPpnService(WsService service, NoticeService noticeService, TopicProducer topicProducer) {
        this.service = service;
        this.noticeService = noticeService;
        this.topicProducer = topicProducer;
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
            return Pattern.compile(doiPattern).matcher(kbart.getTitle_url()).find() ? kbart.getTitle_url().split("doi.org/")[kbart.getTitle_url().split("doi.org/").length - 1] : "";
        }
        if (kbart.getTitle_id() != null && !kbart.getTitle_id().isEmpty()){
            return Pattern.compile(doiPattern).matcher(kbart.getTitle_id()).find() ? kbart.getTitle_id().split("doi.org/")[kbart.getTitle_id().split("doi.org/").length - 1] : "";
        }
        return "";
    }

    //TODO faire une getter qui recuperera la DOI, puis le placer en apramètre dans une méthode feedPpnListFromDOI

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
                if (ppn.getType().equals(TYPE_SUPPORT.ELECTRONIQUE) && checkUrlInNotice(ppn.getPpn(), titleUrl)) {
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

    public boolean checkUrlInNotice(String ppn, String titleUrl) throws IOException, IllegalPpnException, URISyntaxException {
        log.debug("entrée dans checkUrlInNotice " + titleUrl);
        if (titleUrl == null || titleUrl.contains("doi.org")) {
            log.debug("titleUrl null ou contient doi.org");
            return true;
        }
        String domain = Utils.extractDomainFromUrl(titleUrl);
        //récupération notice dans la base pour analyse
        NoticeXml notice = noticeService.getNoticeByPpn(ppn);
        List<Datafield> zones856 = notice.getZoneDollarUWithoutDollar5("856");
        for(Datafield zone : zones856) {
            for (SubField sousZone : zone.getSubFields().stream().filter(sousZone -> sousZone.getCode().equals("u")).collect(Collectors.toList())) {
                if (sousZone.getValue().contains(domain)) {
                    log.debug("Url trouvée dans 856");
                    return true;
                }
            }
        }
        List<Datafield> zone859 = notice.getZoneDollarUWithoutDollar5("859");
        for (Datafield zone : zone859) {
            for (SubField sousZone : zone.getSubFields().stream().filter(sousZone -> sousZone.getCode().equals("u")).collect(Collectors.toList())) {
                if (sousZone.getValue().contains(domain)) {
                    log.debug("Url trouvée dans 859");
                    return true;
                }
            }
        }
        log.debug("Url non trouvée dans notice");
        return false;
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

    public String getBestPpnByScore(LigneKbartDto kbart, String provider, Map<String, Integer> ppnElecResultList, Set<String> ppnPrintResultList) throws BestPpnException, JsonProcessingException {
        Map<String, Integer> ppnElecScore = getMaxValuesFromMap(ppnElecResultList);
        switch (ppnElecScore.size()) {
            case 0:
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
                break;
            case 1:
                return ppnElecScore.keySet().stream().findFirst().get();
            default:
                kbart.setErrorType("Les ppn électroniques " + ppnElecScore.toString() + " ont le même score");
                log.error("Les ppn électroniques " + String.join(", ", ppnElecScore.toString()) + " ont le même score");
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
