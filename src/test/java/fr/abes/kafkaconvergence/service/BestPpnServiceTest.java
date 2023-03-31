package fr.abes.kafkaconvergence.service;

import fr.abes.kafkaconvergence.dto.*;
import fr.abes.kafkaconvergence.exception.IllegalPpnException;
import fr.abes.kafkaconvergence.logger.Logger;
import fr.abes.kafkaconvergence.utils.TYPE_SUPPORT;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest(classes = {BestPpnService.class})
@TestPropertySource(properties = {"score.online.id.to.ppn=10", "score.print.id.to.ppn=8", "score.error.type.notice=6", "score.dat.to.ppn=20"})
class BestPpnServiceTest {

    @Autowired
    BestPpnService bestPpnService;

    @MockBean
    NoticeService noticeService;

    @MockBean
    WsService service;

    @MockBean
    Logger logger;

    @MockBean
    LoggerResultDto loggerResultDto;

    @Value("${score.online.id.to.ppn}")
    long scoreOnlineId2Ppn;

    @Value("${score.print.id.to.ppn}")
    long scorePrintId2Ppn;

    @Value("${score.error.type.notice}")
    long scoreErrorType;

    @Value("${score.dat.to.ppn}")
    long scoreDat2Ppn;

    @Test
    void sortByBestPpn(){
        Map<String, Long> list = new HashMap<>();
        list.put("444444444", Long.valueOf(4));
        list.put("777777777", Long.valueOf(7));
        list.put("222222222", Long.valueOf(2));
        list.put("666666666", Long.valueOf(6));
        List<String> result = bestPpnService.sortByBestPpn(list);
        Assertions.assertEquals(result.get(0), "777777777");
    }

    @Test
    @DisplayName("Test avec cohérence de type")
    void getBestPpnTest01() throws IllegalPpnException, IOException {
        //  Create PpnWithTypeDto for elec
        PpnWithTypeDto ppnWithType1 = new PpnWithTypeDto();
        ppnWithType1.setPpn("100000001");
        ppnWithType1.setType(TYPE_SUPPORT.ELECTRONIQUE);
        PpnWithTypeDto ppnWithType2 = new PpnWithTypeDto();
        ppnWithType2.setPpn("100000002");
        ppnWithType2.setType(TYPE_SUPPORT.ELECTRONIQUE);
        PpnWithTypeDto ppnWithType3 = new PpnWithTypeDto();
        ppnWithType3.setPpn("100000003");
        ppnWithType3.setType(TYPE_SUPPORT.ELECTRONIQUE);
        //  Create a List of PpnWithListDto for elec
        List<PpnWithTypeDto> ppnWithTypeDto = new ArrayList<>();
        ppnWithTypeDto.add(ppnWithType1);
        ppnWithTypeDto.add(ppnWithType2);
        ppnWithTypeDto.add(ppnWithType3);
        //  Create a ResultWsSudocDto for elec
        ResultWsSudocDto resultElec = new ResultWsSudocDto();
        resultElec.setPpns(ppnWithTypeDto);

        //  Create a PpnWithTypeDto for print
        PpnWithTypeDto ppnWithTypePrint = new PpnWithTypeDto();
        ppnWithTypePrint.setPpn("200000001");
        ppnWithTypePrint.setType(TYPE_SUPPORT.IMPRIME);
        //  Create a List of PpnWithListDto for print
        List<PpnWithTypeDto> ppnWithTypePrintDto = new ArrayList<>();
        ppnWithTypePrintDto.add(ppnWithTypePrint);
        //  Create a ResultWsSudocDto for print
        ResultWsSudocDto resultPrint = new ResultWsSudocDto();
        resultPrint.setPpns(ppnWithTypePrintDto);

        //  Create a ResultDat2PpnWebDto
        ResultDat2PpnWebDto resultDat2PpnWeb = new ResultDat2PpnWebDto();
        resultDat2PpnWeb.addPpn("300000001");
        resultDat2PpnWeb.addPpn("300000002");

        //  Create a LigneKbartDto
        LigneKbartDto kbart = new LigneKbartDto();
        kbart.setOnline_identifier("1292-8399");
        kbart.setPrint_identifier("2-84358-095-1");
        kbart.setPublication_type("serial");
        kbart.setDate_monograph_published_online("DateOnline");
        kbart.setPublication_title("Titre");
        kbart.setFirst_author("Auteur");
        kbart.setDate_monograph_published_print("DatePrint");

        //  Mock the WsService
        Mockito.when(service.callOnlineId2Ppn("serial", "1292-8399", "UrlProvider")).thenReturn(resultElec);
        Mockito.when(service.callPrintId2Ppn("serial", "2-84358-095-1", "UrlProvider")).thenReturn(resultPrint);
        Mockito.when(service.callDat2Ppn("serial", "1292-8399", "")).thenReturn(resultDat2PpnWeb);

        List<String> test = bestPpnService.getBestPpn(kbart, "UrlProvider");

        //  Test avec 3 ppns type électronique
        Assertions.assertEquals(test.get(0), "100000001");
    }

    @Test
    @DisplayName("Test avec incohérence de type")
    void getBestPpnTest02() throws IllegalPpnException, IOException {
//        //  Create PpnWithTypeDto for elec
//        PpnWithTypeDto ppnWithType1 = new PpnWithTypeDto();
//        ppnWithType1.setPpn("100000001");
//        ppnWithType1.setType(TYPE_SUPPORT.ELECTRONIQUE);
//        PpnWithTypeDto ppnWithType2 = new PpnWithTypeDto();
//        ppnWithType2.setPpn("100000002");
//        ppnWithType2.setType(TYPE_SUPPORT.ELECTRONIQUE);
//        PpnWithTypeDto ppnWithType3 = new PpnWithTypeDto();
//        ppnWithType3.setPpn("100000003");
//        ppnWithType3.setType(TYPE_SUPPORT.ELECTRONIQUE);
//        //  Create a List of PpnWithListDto for elec
//        List<PpnWithTypeDto> ppnWithTypeDto = new ArrayList<>();
//        ppnWithTypeDto.add(ppnWithType1);
//        ppnWithTypeDto.add(ppnWithType2);
//        ppnWithTypeDto.add(ppnWithType3);
//        //  Create a ResultWsSudocDto for elec
//        ResultWsSudocDto resultElec = new ResultWsSudocDto();
//        resultElec.setPpns(ppnWithTypeDto);
//
//        //  Create a PpnWithTypeDto for print
//        PpnWithTypeDto ppnWithTypePrint = new PpnWithTypeDto();
//        ppnWithTypePrint.setPpn("200000001");
//        ppnWithTypePrint.setType(TYPE_SUPPORT.IMPRIME);
//        //  Create a List of PpnWithListDto for print
//        List<PpnWithTypeDto> ppnWithTypePrintDto = new ArrayList<>();
//        ppnWithTypePrintDto.add(ppnWithTypePrint);
//        //  Create a ResultWsSudocDto for print
//        ResultWsSudocDto resultPrint = new ResultWsSudocDto();
//        resultPrint.setPpns(ppnWithTypePrintDto);
//
//        //  Create a ResultDat2PpnWebDto
//        ResultDat2PpnWebDto resultDat2PpnWeb = new ResultDat2PpnWebDto();
//        resultDat2PpnWeb.addPpn("300000001");
//        resultDat2PpnWeb.addPpn("300000002");
//
//        //  Create a LigneKbartDto
//        LigneKbartDto kbart = new LigneKbartDto();
//        kbart.setOnline_identifier("1292-8399");
//        kbart.setPrint_identifier("2-84358-095-1");
//        kbart.setPublication_type("serial");
//        kbart.setDate_monograph_published_online("DateOnline");
//        kbart.setPublication_title("Titre");
//        kbart.setFirst_author("Auteur");
//        kbart.setDate_monograph_published_print("DatePrint");
//
//        //  Mock the WsService
//        Mockito.when(service.callOnlineId2Ppn("serial", "1292-8399", "UrlProvider")).thenReturn(resultElec);
//        Mockito.when(service.callPrintId2Ppn("serial", "2-84358-095-1", "UrlProvider")).thenReturn(resultPrint);
//        Mockito.when(service.callDat2Ppn("serial", "1292-8399", "")).thenReturn(resultDat2PpnWeb);
//
//        List<String> test = bestPpnService.getBestPpn(kbart, "UrlProvider");
//
//        //  Test avec 3 ppns type électronique
//        Assertions.assertEquals(test.get(0), "100000001");
    }
}
