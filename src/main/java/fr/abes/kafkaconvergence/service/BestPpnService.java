package fr.abes.kafkaconvergence.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import fr.abes.kafkaconvergence.dto.LigneKbartDto;
import fr.abes.kafkaconvergence.dto.PpnWithTypeDto;
import fr.abes.kafkaconvergence.dto.ResultDat2PpnWebDto;
import fr.abes.kafkaconvergence.dto.ResultWsSudocDto;
import fr.abes.kafkaconvergence.entity.PpnResultList;
import fr.abes.kafkaconvergence.entity.basexml.notice.NoticeXml;
import fr.abes.kafkaconvergence.exception.IllegalPpnException;
import fr.abes.kafkaconvergence.utils.TYPE_SUPPORT;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Getter
@Slf4j
public class BestPpnService {
    private final WsService service;

    private PpnResultList ppnResultList;

    private List<String> ppnPrintListFromOnlineId2Ppn;

    private List<String> ppnPrintListFromPrintId2Ppn;

    private List<String> ppnPrintListFromDat2Ppn;

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
        this.ppnPrintListFromOnlineId2Ppn = new ArrayList<>();
        this.ppnPrintListFromPrintId2Ppn = new ArrayList<>();
        this.ppnPrintListFromDat2Ppn = new ArrayList<>();
    }

//    public List<String> sortByBestPpn(Map<String, Long> list) {
//        List<String> result = new ArrayList<>();
//        result.add(Collections.max(list.entrySet(), Map.Entry.comparingByValue()).getKey());
//        return result;
//    }

    public List<String> getBestPpn(LigneKbartDto kbart, String provider) throws IOException, IllegalPpnException {
        this.ppnResultList = new PpnResultList();
        if (!kbart.getOnline_identifier().isEmpty() && !kbart.getPublication_type().isEmpty()) {
            feedPpnListFromOnline(kbart, provider);
            feedPpnListFromPrint(kbart, provider);
            if (ppnResultList.getMapPpnScore().isEmpty()) {
                feedPpnListFromDat(kbart);
            }
        }
        if (ppnResultList.getMapPpnScore().isEmpty()) {
            log.error("BestPpn" + kbart.toString() + "Aucun bestPpn trouvé.");
        }
        return new ArrayList<>(ppnResultList.getMapPpnScore().keySet());
    }

    public void feedPpnListFromOnline(LigneKbartDto kbart, String provider) throws JsonProcessingException {
        ResultWsSudocDto resultCallOnlineId2Ppn = service.callOnlineId2Ppn(kbart.getPublication_type(), kbart.getOnline_identifier(), provider);
        if (!resultCallOnlineId2Ppn.getPpns().isEmpty()) {
            long nbPpnElec = resultCallOnlineId2Ppn.getPpns().stream().filter(ppnWithTypeDto -> ppnWithTypeDto.getType().equals(TYPE_SUPPORT.ELECTRONIQUE)).count();
            for (PpnWithTypeDto ppn : resultCallOnlineId2Ppn.getPpns()) {
                if (ppn.getType().equals(TYPE_SUPPORT.ELECTRONIQUE)) {
                    this.ppnResultList.addPpn(ppn.getPpn(), scoreOnlineId2Ppn / nbPpnElec);
                } else if (ppn.getType().equals(TYPE_SUPPORT.IMPRIME)) {
                    this.ppnPrintListFromOnlineId2Ppn.add(ppn.getPpn());
                }
            }
            if (this.ppnPrintListFromOnlineId2Ppn.size() > 1) {
                log.error("OnlineId2Ppn " + kbart.toString() + "Plus d'un ppn de type imprimé a été trouvé : " + String.join(", ", this.ppnPrintListFromOnlineId2Ppn));
            }
            if (nbPpnElec > 1) {
                log.error("OnlineId2Ppn " + kbart.toString() + " " + resultCallOnlineId2Ppn.getErreurs().stream().map(String::toString).collect(Collectors.joining(", ")));
            }
        }
    }

    public void feedPpnListFromPrint(LigneKbartDto kbart, String provider) throws JsonProcessingException {
        ResultWsSudocDto resultPrintId2Ppn = service.callPrintId2Ppn(kbart.getPublication_type(), kbart.getPrint_identifier(), provider);
        if (!resultPrintId2Ppn.getPpns().isEmpty()) {
            long nbPpnElec = resultPrintId2Ppn.getPpns().stream().filter(ppnWithTypeDto -> ppnWithTypeDto.getType().equals(TYPE_SUPPORT.ELECTRONIQUE)).count();
            for (PpnWithTypeDto ppn : resultPrintId2Ppn.getPpns()) {
                if (ppn.getType().equals(TYPE_SUPPORT.ELECTRONIQUE)) {
                    this.ppnResultList.addPpn(ppn.getPpn(), scorePrintId2Ppn / nbPpnElec);
                } else if (ppn.getType().equals(TYPE_SUPPORT.IMPRIME)) {
                    this.ppnPrintListFromPrintId2Ppn.add(ppn.getPpn());
                }
            }
            if (this.ppnPrintListFromPrintId2Ppn.size() > 1) {
                log.error("PrintId2Ppn " + kbart.toString() + "Plus d'un ppn de type imprimé a été trouvé : " + String.join(", ", this.ppnPrintListFromPrintId2Ppn));
            }
            if (nbPpnElec > 1) {
                log.error("PrintId2Ppn " + kbart.toString() + " " + resultPrintId2Ppn.getErreurs().stream().map(String::toString).collect(Collectors.joining(", ")));
            }
        }
    }

    public void feedPpnListFromDat(LigneKbartDto kbart) throws IOException, IllegalPpnException {
        if (!kbart.getDate_monograph_published_online().isEmpty()) {
            ResultDat2PpnWebDto resultDat2PpnWeb = service.callDat2Ppn(kbart.getDate_monograph_published_online(), kbart.getAuthor(), kbart.getPublication_title());
            for (String ppn : resultDat2PpnWeb.getPpns()) {
                NoticeXml notice = noticeService.getNoticeByPpn(ppn);
                if (notice.isNoticeElectronique()) {
                    this.ppnResultList.addPpn(ppn, scoreDat2Ppn);
                }
            }
        }
        if (this.ppnResultList.getMapPpnScore().isEmpty() && !kbart.getDate_monograph_published_print().isEmpty()) {
            ResultDat2PpnWebDto resultDat2PpnWeb = service.callDat2Ppn(kbart.getDate_monograph_published_print(), kbart.getAuthor(), kbart.getPublication_title());
            for (String ppn : resultDat2PpnWeb.getPpns()) {
                NoticeXml notice = noticeService.getNoticeByPpn(ppn);
                if (notice.isNoticeImprimee()) {
                    this.ppnPrintListFromDat2Ppn.add(ppn);
                }
            }
            if (this.ppnPrintListFromDat2Ppn.size() > 1) {
                log.error("Dat2Ppn " + kbart.toString() + "Plus d'un ppn de type imprimé a été trouvé : " + String.join(", ", this.ppnPrintListFromDat2Ppn));
            }
        }
    }
}
