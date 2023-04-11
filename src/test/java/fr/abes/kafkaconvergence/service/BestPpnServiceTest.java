package fr.abes.kafkaconvergence.service;

import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import fr.abes.kafkaconvergence.dto.LigneKbartDto;
import fr.abes.kafkaconvergence.dto.PpnWithTypeDto;
import fr.abes.kafkaconvergence.dto.ResultDat2PpnWebDto;
import fr.abes.kafkaconvergence.dto.ResultWsSudocDto;
import fr.abes.kafkaconvergence.entity.basexml.notice.NoticeXml;
import fr.abes.kafkaconvergence.exception.IllegalPpnException;
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
import java.util.List;

@SpringBootTest(classes = {BestPpnService.class})
@TestPropertySource(properties = {"score.online.id.to.ppn=10", "score.print.id.to.ppn=8", "score.error.type.notice=6", "score.dat.to.ppn=20"})
class BestPpnServiceTest {

    @Autowired
    BestPpnService bestPpnService;

    @MockBean
    NoticeService noticeService;

    @MockBean
    WsService service;

    @Value("${score.online.id.to.ppn}")
    long scoreOnlineId2Ppn;

    @Value("${score.print.id.to.ppn}")
    long scorePrintId2Ppn;

    @Value("${score.error.type.notice}")
    long scoreErrorType;

    @Value("${score.dat.to.ppn}")
    long scoreDat2Ppn;

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
        List<String> result = bestPpnService.getBestPpn(kbart, provider);

        //  Vérification
        Assertions.assertEquals(result.size(), 2);
        Assertions.assertEquals(bestPpnService.getPpnPrintListFromOnlineId2Ppn().size(), 1);
        Assertions.assertEquals(bestPpnService.getPpnPrintListFromOnlineId2Ppn().get(0), "100000003");
        Assertions.assertEquals(2, result.size());
        Assertions.assertEquals(result.get(0), "100000001");
        Assertions.assertEquals(result.get(1), "100000002");
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
        List<String> result = bestPpnService.getBestPpn(kbart, provider);

        //  Vérification
        Assertions.assertEquals(result.size(), 1);
        Assertions.assertEquals(bestPpnService.getPpnPrintListFromPrintId2Ppn().get(0), "200000002");
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(result.get(0), "200000001");
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
        List<String> result = bestPpnService.getBestPpn(kbart, provider);

        //  Test avec Notice électronique
        Assertions.assertEquals(result.size(), 1);
        Assertions.assertEquals(result.get(0), "300000001");

        //  Create a ResultDat2PpnWebDto
        ResultDat2PpnWebDto resultDat2PpnWeb2 = new ResultDat2PpnWebDto();
        resultDat2PpnWeb2.addPpn("300000002");

        //  Test avec Notice monographie
        Mockito.when(service.callDat2Ppn(kbart.getDate_monograph_published_print(), kbart.getFirst_author(), kbart.getPublication_title())).thenReturn(resultDat2PpnWeb2);
        Mockito.when(noticeService.getNoticeByPpn("300000001")).thenReturn(noticePrint);
        Mockito.when(noticeService.getNoticeByPpn("300000002")).thenReturn(noticePrint);

        //  Appel du service
        List<String> result2 = bestPpnService.getBestPpn(kbart, provider);
        //  Vérification
        Assertions.assertEquals(result2.size(), 0);
        Assertions.assertEquals(bestPpnService.getPpnPrintListFromDat2Ppn().size(), 1);
        Assertions.assertEquals(bestPpnService.getPpnPrintListFromDat2Ppn().get(0), "300000002");
    }
}
