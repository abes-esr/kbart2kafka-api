package fr.abes.kafkaconvergence.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.abes.kafkaconvergence.dto.ResultWsSudocDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;

@Service
@Slf4j
public class WsService {
    @Value("${url.onlineId2Ppn}")
    private String urlOnlineId2Ppn;

    @Value("${url.printId2Ppn")
    private String urlPrintId2Ppn;

    private final RestTemplate restTemplate;
    private final HttpHeaders headers;

    private final ObjectMapper mapper;

    public WsService(ObjectMapper mapper, RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        this.mapper = mapper;
    }


    public String postCall(String url, String requestJson) {
        HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);

        restTemplate.getMessageConverters()
                .add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));
        return restTemplate.postForObject(url, entity, String.class);
    }

    public String getCall(String url, String... params) {
        StringBuilder formedUrl = new StringBuilder(url);
        for (String param : params) {
            formedUrl.append("/");
            formedUrl.append(param);
        }
        log.info(formedUrl.toString());
        return restTemplate.getForObject(formedUrl.toString(), String.class);
    }

    public ResultWsSudocDto callOnlineId2Ppn(String type, String id, @Nullable String provider) throws JsonProcessingException {
        return mapper.readValue(getCall(urlOnlineId2Ppn, type, id, provider), ResultWsSudocDto.class);
    }

    public ResultWsSudocDto callPrintId2Ppn(String type, String id) throws JsonProcessingException {
        return mapper.readValue(getCall(urlPrintId2Ppn, type, id), ResultWsSudocDto.class);
    }


}
