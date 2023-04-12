package fr.abes.kafkaconvergence.service;

import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import fr.abes.kafkaconvergence.dto.LigneKbartDto;
import fr.abes.kafkaconvergence.dto.PpnWithTypeDto;
import fr.abes.kafkaconvergence.dto.ResultDat2PpnWebDto;
import fr.abes.kafkaconvergence.dto.ResultWsSudocDto;
import fr.abes.kafkaconvergence.entity.PpnResultList;
import fr.abes.kafkaconvergence.entity.basexml.notice.NoticeXml;
import fr.abes.kafkaconvergence.exception.IllegalPpnException;
import fr.abes.kafkaconvergence.exception.ScoreException;
import fr.abes.kafkaconvergence.utils.TYPE_SUPPORT;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.Resource;
import org.springframework.test.context.TestPropertySource;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest(classes = {BestPpnService.class})
class BestPpnServiceTest {

    @Autowired
    BestPpnService bestPpnService;

    @MockBean
    NoticeService noticeService;

    @MockBean
    WsService service;

    @Value("classpath:143519379.xml")
    private Resource xmlFileNoticePrint;

    @Value("classpath:143519380.xml")
    private Resource xmlFileNoticeElec;

    private NoticeXml noticePrint;

    private NoticeXml noticeElec;

    @BeforeEach
    void init() throws IOException {
        String xml = IOUtils.toString(new FileInputStream(xmlFileNoticeElec.getFile()), StandardCharsets.UTF_8);
        JacksonXmlModule module = new JacksonXmlModule();
        module.setDefaultUseWrapper(false);
        XmlMapper mapper = new XmlMapper(module);
        this.noticeElec = mapper.readValue(xml, NoticeXml.class);

        String xml2 = IOUtils.toString(new FileInputStream(xmlFileNoticePrint.getFile()), StandardCharsets.UTF_8);
        JacksonXmlModule module2 = new JacksonXmlModule();
        module2.setDefaultUseWrapper(false);
        XmlMapper mapper2 = new XmlMapper(module2);
        this.noticePrint = mapper2.readValue(xml2, NoticeXml.class);
    }

    @Test
    @DisplayName("Test feedPpnListFromOnline")
    void getBestPpnTest01() throws IllegalPpnException, IOException {
        String provider = "urlProvider";
        //  Create PpnWithTypeDto for elec
        PpnWithTypeDto ppnWithType1 = new PpnWithTypeDto();
        ppnWithType1.setPpn("100000001");
        ppnWithType1.setType(TYPE_SUPPORT.ELECTRONIQUE);
        PpnWithTypeDto ppnWithType2 = new PpnWithTypeDto();
        ppnWithType2.setPpn("100000002");
        ppnWithType2.setType(TYPE_SUPPORT.ELECTRONIQUE);
        PpnWithTypeDto ppnWithType3 = new PpnWithTypeDto();
        ppnWithType3.setPpn("100000003");
        ppnWithType3.setType(TYPE_SUPPORT.IMPRIME);
        //  Create a List of PpnWithListDto for elec
        List<PpnWithTypeDto> ppnWithTypeDto = new ArrayList<>();
        ppnWithTypeDto.add(ppnWithType1);
        ppnWithTypeDto.add(ppnWithType2);
        ppnWithTypeDto.add(ppnWithType3);
        //  Create a ResultWsSudocDto for elec
        ResultWsSudocDto resultElec = new ResultWsSudocDto();
        resultElec.setPpns(ppnWithTypeDto);

        //  Create a List of PpnWithListDto for print
        List<PpnWithTypeDto> ppnWithTypePrintDto = new ArrayList<>();
        //  Create a ResultWsSudocDto for print
        ResultWsSudocDto resultPrint = new ResultWsSudocDto();
        resultPrint.setPpns(ppnWithTypePrintDto);

       //  Create a LigneKbartDto
        LigneKbartDto kbart = new LigneKbartDto();
        kbart.setOnline_identifier("1292-8399");
        kbart.setPrint_identifier("2-84358-095-1");
        kbart.setPublication_type("serial");
        kbart.setDate_monograph_published_online("DateOnline");
        kbart.setPublication_title("Titre");
        kbart.setFirst_author("Auteur");
        kbart.setDate_monograph_published_print("DatePrint");

        //  Mock
        Mockito.when(service.callOnlineId2Ppn(kbart.getPublication_type(), kbart.getOnline_identifier(), provider)).thenReturn(resultElec);
        Mockito.when(service.callPrintId2Ppn(kbart.getPublication_type(), kbart.getPrint_identifier(), provider)).thenReturn(resultPrint);

        //  Appel du service
        PpnResultList result = bestPpnService.getBestPpn(kbart, provider);

        //  Vérification
        Assertions.assertEquals(3, (long) result.getMapPpnScore().keySet().size());
        Assertions.assertEquals(1 ,result.getMapPpnScore().keySet().stream().filter(ppn -> ppn.getType().equals(TYPE_SUPPORT.IMPRIME)).count());
        Assertions.assertEquals(2,result.getMapPpnScore().keySet().stream().filter(ppn -> ppn.getType().equals(TYPE_SUPPORT.ELECTRONIQUE)).count());
        Assertions.assertEquals("100000003", result.getMapPpnScore().keySet().stream().filter(ppn -> ppn.getType().equals(TYPE_SUPPORT.IMPRIME)).findFirst().get().getPpn());
        Assertions.assertEquals(5, result.getMapPpnScore().entrySet().stream().filter(ppn -> ppn.getKey().getPpn().equals("100000001")).findFirst().get().getValue());
        Assertions.assertEquals(5, result.getMapPpnScore().entrySet().stream().filter(ppn -> ppn.getKey().getPpn().equals("100000002")).findFirst().get().getValue());
    }

    @Test
    @DisplayName("Test feedPpnListFromPrint")
    void getBestPpnTest02() throws IllegalPpnException, IOException {
        String provider = "urlProvider";
        //  Create a List of PpnWithListDto for elec
        List<PpnWithTypeDto> ppnWithTypeDto = new ArrayList<>();
        //  Create a ResultWsSudocDto for elec
        ResultWsSudocDto resultElec = new ResultWsSudocDto();
        resultElec.setPpns(ppnWithTypeDto);

        //  Create a PpnWithTypeDto for print
        PpnWithTypeDto ppnWithTypePrint1 = new PpnWithTypeDto();
        ppnWithTypePrint1.setPpn("200000001");
        ppnWithTypePrint1.setType(TYPE_SUPPORT.ELECTRONIQUE);
        PpnWithTypeDto ppnWithTypePrint2 = new PpnWithTypeDto();
        ppnWithTypePrint2.setPpn("200000002");
        ppnWithTypePrint2.setType(TYPE_SUPPORT.IMPRIME);
        //  Create a List of PpnWithListDto for print
        List<PpnWithTypeDto> ppnWithTypePrintDto = new ArrayList<>();
        ppnWithTypePrintDto.add(ppnWithTypePrint1);
        ppnWithTypePrintDto.add(ppnWithTypePrint2);
        //  Create a ResultWsSudocDto for print
        ResultWsSudocDto resultPrint = new ResultWsSudocDto();
        resultPrint.setPpns(ppnWithTypePrintDto);

        //  Create a LigneKbartDto
        LigneKbartDto kbart = new LigneKbartDto();
        kbart.setOnline_identifier("1292-8399");
        kbart.setPrint_identifier("2-84358-095-1");
        kbart.setPublication_type("serial");
        kbart.setDate_monograph_published_online("DateOnline");
        kbart.setPublication_title("Titre");
        kbart.setFirst_author("Auteur");
        kbart.setDate_monograph_published_print("DatePrint");

        //  Mock
        Mockito.when(service.callOnlineId2Ppn(kbart.getPublication_type(), kbart.getOnline_identifier(), provider)).thenReturn(resultElec);
        Mockito.when(service.callPrintId2Ppn(kbart.getPublication_type(), kbart.getPrint_identifier(), provider)).thenReturn(resultPrint);

        //  Appel du service
        PpnResultList result = bestPpnService.getBestPpn(kbart, provider);

        //  Vérification
        Assertions.assertEquals(2, (long) result.getMapPpnScore().keySet().size());
        Assertions.assertEquals(1, result.getMapPpnScore().keySet().stream().filter(ppn -> ppn.getType().equals(TYPE_SUPPORT.IMPRIME)).count());
        Assertions.assertEquals(1, result.getMapPpnScore().keySet().stream().filter(ppn -> ppn.getType().equals(TYPE_SUPPORT.ELECTRONIQUE)).count());
        Assertions.assertEquals("200000002", result.getMapPpnScore().keySet().stream().filter(ppn -> ppn.getType().equals(TYPE_SUPPORT.IMPRIME)).findFirst().get().getPpn());
        Assertions.assertEquals(8, result.getMapPpnScore().entrySet().stream().filter(ppn -> ppn.getKey().getPpn().equals("200000001")).findFirst().get().getValue());
    }

    @Test
    @DisplayName("Test feedPpnListFromDat")
    void getBestPpnTest03() throws IllegalPpnException, IOException {
        String provider = "urlProvider";
        //  Create a List of PpnWithListDto for elec
        List<PpnWithTypeDto> ppnWithTypeDto = new ArrayList<>();
        //  Create a ResultWsSudocDto for elec
        ResultWsSudocDto resultElec = new ResultWsSudocDto();
        resultElec.setPpns(ppnWithTypeDto);

        //  Create a List of PpnWithListDto for print
        List<PpnWithTypeDto> ppnWithTypePrintDto = new ArrayList<>();
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

        //  Mock
        Mockito.when(service.callOnlineId2Ppn(kbart.getPublication_type(), kbart.getOnline_identifier(), provider)).thenReturn(resultElec);
        Mockito.when(service.callPrintId2Ppn(kbart.getPublication_type(), kbart.getPrint_identifier(), provider)).thenReturn(resultPrint);
        Mockito.when(service.callDat2Ppn(kbart.getDate_monograph_published_online(), kbart.getFirst_author(), kbart.getPublication_title())).thenReturn(resultDat2PpnWeb);
        Mockito.when(noticeService.getNoticeByPpn("300000001")).thenReturn(noticeElec);
        Mockito.when(noticeService.getNoticeByPpn("300000002")).thenReturn(noticePrint);

        //  Appel du service
        PpnResultList result = bestPpnService.getBestPpn(kbart, provider);

        //  Test avec Notice électronique
        Assertions.assertEquals(1, (long) result.getMapPpnScore().keySet().size());
        Assertions.assertEquals(1, result.getMapPpnScore().entrySet().stream().filter(ppn -> ppn.getKey().getType().equals(TYPE_SUPPORT.ELECTRONIQUE)).count());
        Assertions.assertEquals(20, result.getMapPpnScore().entrySet().stream().filter(ppn -> ppn.getKey().getPpn().equals("300000001")).findFirst().get().getValue());

        //  Create a ResultDat2PpnWebDto
        ResultDat2PpnWebDto resultDat2PpnWeb2 = new ResultDat2PpnWebDto();
        resultDat2PpnWeb2.addPpn("300000002");

        //  Test avec Notice monographie
        Mockito.when(service.callDat2Ppn(kbart.getDate_monograph_published_print(), kbart.getFirst_author(), kbart.getPublication_title())).thenReturn(resultDat2PpnWeb2);
        //Mockito.when(noticeService.getNoticeByPpn("300000001")).thenReturn(noticePrint);
        Mockito.when(noticeService.getNoticeByPpn("300000002")).thenReturn(noticePrint);

        //  Appel du service
        PpnResultList result2 = bestPpnService.getBestPpn(kbart, provider);
        //  Vérification
        Assertions.assertEquals(1, (long) result2.getMapPpnScore().keySet().size());
        Assertions.assertEquals(1, result2.getMapPpnScore().keySet().stream().filter(ppn -> ppn.getType().equals(TYPE_SUPPORT.IMPRIME)).count());
        Assertions.assertEquals(0L, result.getMapPpnScore().entrySet().stream().filter(ppn -> ppn.getKey().getPpn().equals("300000002")).findFirst().get().getValue());
    }

    @Test
    @DisplayName("test best ppn with score : 1 seule notice électronique")
    void bestPpnWithScoreTest1() throws ScoreException {
        PpnResultList in = new PpnResultList();
        Map<PpnWithTypeDto, Long> ppns = new HashMap<>();
        ppns.put(new PpnWithTypeDto("111111111", TYPE_SUPPORT.ELECTRONIQUE), 10L);
        in.setMapPpnScore(ppns);

        List<String> result = bestPpnService.getBestPpnByScore(in);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("111111111", result.get(0));
    }

    @Test
    @DisplayName("test best ppn with score : 2 notices électroniques avec score différnt")
    void bestPpnWithScoreTest2() throws ScoreException {
        PpnResultList in = new PpnResultList();
        Map<PpnWithTypeDto, Long> ppns = new HashMap<>();
        ppns.put(new PpnWithTypeDto("111111111", TYPE_SUPPORT.ELECTRONIQUE), 10L);
        ppns.put(new PpnWithTypeDto("222222222", TYPE_SUPPORT.ELECTRONIQUE), 5L);
        in.setMapPpnScore(ppns);

        List<String> result = bestPpnService.getBestPpnByScore(in);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("111111111", result.get(0));
    }

    @Test
    @DisplayName("test best ppn with score : 2 notices électroniques avec même score")
    void bestPpnWithScoreTest3() throws ScoreException {
        PpnResultList in = new PpnResultList();
        Map<PpnWithTypeDto, Long> ppns = new HashMap<>();
        ppns.put(new PpnWithTypeDto("111111111", TYPE_SUPPORT.ELECTRONIQUE), 10L);
        ppns.put(new PpnWithTypeDto("222222222", TYPE_SUPPORT.ELECTRONIQUE), 10L);
        in.setMapPpnScore(ppns);

        List<String> result = bestPpnService.getBestPpnByScore(in);
        Assertions.assertEquals(2, result.size());
        Assertions.assertEquals("111111111", result.get(0));
        Assertions.assertEquals("222222222", result.get(1));
    }

    @Test
    @DisplayName("test best ppn with score : 2 notices dont une électronique")
    void bestPpnWithScoreTest4() throws ScoreException {
        PpnResultList in = new PpnResultList();
        Map<PpnWithTypeDto, Long> ppns = new HashMap<>();
        ppns.put(new PpnWithTypeDto("111111111", TYPE_SUPPORT.ELECTRONIQUE), 10L);
        ppns.put(new PpnWithTypeDto("222222222", TYPE_SUPPORT.IMPRIME), 5L);
        in.setMapPpnScore(ppns);

        List<String> result = bestPpnService.getBestPpnByScore(in);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("111111111", result.get(0));
    }

    @Test
    void testMax1(){
        Map<String, Integer> map = new HashMap<>();
        map.put("1", 10);
        map.put("2", 20);
        Map<String, Integer> result = bestPpnService.maxUsingIteration(map);
        Assertions.assertEquals(1 ,result.keySet().size());
        Assertions.assertEquals(10 ,result.get("1"));
    }

    @Test
    void testMax2(){
        Map<String, Integer> map = new HashMap<>();
        map.put("1", 10);
        map.put("2", 20);
        map.put("3", 10);
        Map<String, Integer> result = bestPpnService.maxUsingIteration(map);
        Assertions.assertEquals(2 ,result.keySet().size());
        Assertions.assertEquals(10 ,result.get("1"));
        Assertions.assertEquals(10 ,result.get("3"));
    }
}
