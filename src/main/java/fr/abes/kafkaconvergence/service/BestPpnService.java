package fr.abes.kafkaconvergence.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import fr.abes.kafkaconvergence.dto.*;
import fr.abes.kafkaconvergence.entity.PpnResultList;
import fr.abes.kafkaconvergence.entity.basexml.notice.NoticeXml;
import fr.abes.kafkaconvergence.exception.IllegalPpnException;
import fr.abes.kafkaconvergence.utils.TYPE_SUPPORT;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Getter
@RequiredArgsConstructor
@Slf4j
public class BestPpnService {
    private final WsService service;

    private LoggerResultDto loggerResultDto;

    private PpnResultList ppnResultList;

    private List<String> ppnPrintListFromOnlineId2Ppn;

    private List<String> ppnPrintListFromPrintId2Ppn;

    @Value("${score.online.id.to.ppn}")
    private long scoreOnlineId2Ppn;

    @Value("${score.print.id.to.ppn}")
    private long scorePrintId2Ppn;

    @Value("${score.error.type.notice}")
    private long scoreErrorType;

    @Value("${score.dat.to.ppn}")
    private long scoreDat2Ppn;

    @Autowired
    private NoticeService noticeService;

    public List<String> sortByBestPpn(Map<String, Long> list){
        List<String> result = new ArrayList<>();
        result.add(Collections.max(list.entrySet(), Map.Entry.comparingByValue()).getKey());
        return result;
    }

    public List<String> getBestPpn(LigneKbartDto kbart, String provider) throws IOException, IllegalPpnException {
        List<String> result = new ArrayList<>();
        this.ppnResultList = new PpnResultList();
        this.ppnPrintListFromOnlineId2Ppn = new ArrayList<>();
        this.ppnPrintListFromPrintId2Ppn = new ArrayList<>();
        if (!kbart.getOnline_identifier().isEmpty() && !kbart.getPublication_type().isEmpty()) {
            feedPpnListFromOnline(kbart, provider);
            feedPpnListFromPrint(kbart, provider);
            if (ppnResultList.getPpnList().isEmpty()) {
                feedPpnListFromDat(kbart.getDate_monograph_published_online(), kbart.getPublication_title(), kbart.getAuthor(), kbart.getDate_monograph_published_print());
            }
        }
        if(!ppnResultList.getPpnList().isEmpty()){
            result = sortByBestPpn(ppnResultList.getPpnList());
        }

        return result;
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
            if(nbPpnElec > 1){
                log.error("OnlineId2Ppn " + kbart.toString() + " " + resultCallOnlineId2Ppn.getErreurs().stream().map(String::toString).collect(Collectors.joining(", ")));
                //  TODO logger.info("onlineId2Ppn à renvoyé" + nbPpnElec + "notices electroniques"); ENCORE DES INFOS UTILES ?
            }
        }
    }

    public void feedPpnListFromPrint(LigneKbartDto kbart, String provider) throws JsonProcessingException {
        ResultWsSudocDto resultPrintId2Ppn = service.callPrintId2Ppn(kbart.getPublication_type(), kbart.getPrint_identifier(), provider);
        if(!resultPrintId2Ppn.getPpns().isEmpty()) {
            long nbPpnElec = resultPrintId2Ppn.getPpns().stream().filter(ppnWithTypeDto -> ppnWithTypeDto.getType().equals(TYPE_SUPPORT.ELECTRONIQUE)).count();
                for (PpnWithTypeDto ppn : resultPrintId2Ppn.getPpns()) {
                    if (ppn.getType().equals(TYPE_SUPPORT.ELECTRONIQUE)) {
                        this.ppnResultList.addPpn(ppn.getPpn(), scorePrintId2Ppn /nbPpnElec);
                    } else if (ppn.getType().equals(TYPE_SUPPORT.IMPRIME)) {
                        this.ppnPrintListFromPrintId2Ppn.add(ppn.getPpn());
                    }
                }
            if(nbPpnElec > 1){
                log.error("PrintId2Ppn " + kbart.toString() + " " + resultPrintId2Ppn.getErreurs().stream().map(String::toString).collect(Collectors.joining(", ")));
                //  TODO gestion des logs -> log.info("PrintId2Ppn à renvoyé" + nbPpnElec + "notices electroniques"); ENCORE DES INFOS UTILES ?
            }
        }
    }

    public void feedPpnListFromDat(String monographPublishedOnline, String publicationTitle, String author, String dateMonographPublishedPrint) throws JsonProcessingException, IOException, IllegalPpnException {
        this.loggerResultDto = new LoggerResultDto();
        this.loggerResultDto.setServiceName("dat2Ppn");
        if (!monographPublishedOnline.isEmpty()){
            ResultDat2PpnWebDto resultDat2PpnWeb = service.callDat2Ppn(monographPublishedOnline, author, publicationTitle);
            for (String ppn : resultDat2PpnWeb.getPpns()) {
                NoticeXml notice = noticeService.getNoticeByPpn(ppn);
                if (notice.isNoticeElectronique()) {
                    this.ppnResultList.addPpn(ppn, scoreDat2Ppn);
                }
            }
        }
        if (this.ppnResultList.getPpnList().isEmpty() && !dateMonographPublishedPrint.isEmpty()) {
            ResultDat2PpnWebDto resultDat2PpnWeb = service.callDat2Ppn(dateMonographPublishedPrint, author, publicationTitle);
            for (String ppn : resultDat2PpnWeb.getPpns()) {
                NoticeXml notice = noticeService.getNoticeByPpn(ppn);
                if (notice.isNoticeImprimee()) {
                    //TODO gérer cas notice imprimée
                }
            }
        } else {
            // TODO si toujours aucune notice n’est retournée, ajouter un message informant de l’absence de PPN sur ce kbart.
        }
    }

    private void sendLogIfError() throws JsonProcessingException {
        if(!this.loggerResultDto.getMessages().isEmpty()){
            log.error(String.valueOf(this.loggerResultDto.getLigneKbartDto().hashCode()), this.loggerResultDto);
        }
    }
}
