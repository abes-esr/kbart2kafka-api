package fr.abes.kafkaconvergence.service;

import fr.abes.kafkaconvergence.dto.LoggerResultDto;
import fr.abes.kafkaconvergence.logger.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest(classes = {BestPpnService.class})
@TestPropertySource(properties = {"score.online.id.to.ppn=10", "score.print.id.to.ppn=8", "score.error.type.notice=6", "score.dat.to.ppn=20"})
class BestPpnServiceTest {

    @Autowired
    BestPpnService service;

    @MockBean
    NoticeService noticeService;

    @MockBean
    WsService wsService;

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
    void getList(){
        Map<String, Long> list = new HashMap<>();
        list.put("444444444", Long.valueOf(4));
        list.put("777777777", Long.valueOf(7));
        list.put("222222222", Long.valueOf(2));
        list.put("666666666", Long.valueOf(6));
        List<String> result = service.sortByBestPpn(list);
        Assertions.assertEquals(result.get(0), "777777777");
    }

    @Test
    void getBestPpn() {
    }

    @Test
    void feedPpnListFromOnline() {
    }

    @Test
    void feedPpnListFromPrint() {
    }

    @Test
    void feedPpnListFromDat() {
    }
}
