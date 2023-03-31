package fr.abes.kafkaconvergence.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import fr.abes.kafkaconvergence.dto.LigneKbartDto;
import fr.abes.kafkaconvergence.dto.LoggerResultDto;
import fr.abes.kafkaconvergence.dto.PpnWithTypeDto;
import fr.abes.kafkaconvergence.dto.ResultWsSudocDto;
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
import java.lang.reflect.Field;
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
    @DisplayName("feedPpnListFromOnline")
    void getBestPpn() throws IllegalPpnException, IOException, NoSuchFieldException {
        //  Create a PpnWithTypeDto
        PpnWithTypeDto ppnWithType = new PpnWithTypeDto();
        ppnWithType.setPpn("111111111");
        ppnWithType.setType(TYPE_SUPPORT.ELECTRONIQUE);
        //  Create a List of PpnWithListDto
        List<PpnWithTypeDto> ppnWithTypeDto = new ArrayList<>();
        ppnWithTypeDto.add(ppnWithType);
        //  Create a ResultWsSudocDto
        ResultWsSudocDto result = new ResultWsSudocDto();
        result.setPpns(ppnWithTypeDto);

        //  Create a LigneKbartDto
        LigneKbartDto kbart = new LigneKbartDto();
        kbart.setOnline_identifier("1292-8399");
        kbart.setPublication_type("serial");
        kbart.setDate_monograph_published_online("DateOnline");
        kbart.setPublication_title("Titre");
        kbart.setFirst_author("Auteur");
        kbart.setDate_monograph_published_print("DatePrint");

        //  Mock the WsService
        Mockito.when(service.callOnlineId2Ppn("serial", "1292-8399", "UrlProvider")).thenReturn(result);
//        Mockito.when(service.callPrintId2Ppn("serial", "1292-8399", "UrlProvider")).thenReturn(result);
//        Mockito.when(service.callDat2Ppn("serial", "1292-8399", "")).thenReturn(result);

        Field field = bestPpnService.getClass().getDeclaredField("ppnElecList");
        field.setAccessible(true);

        List<String> test = bestPpnService.getBestPpn(kbart, "UrlProvider");

        //  Test avec 1 ppn type électronique
        Assertions.assertEquals(test.get(0), result.getPpns().get(0).getPpn());

        //  Test avec 3 ppns type électronique

        // Test avec 1 ppn type imprimé


    }
}
