package fr.abes.kafkaconvergence.service;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import fr.abes.kafkaconvergence.entity.basexml.NoticesBibio;
import fr.abes.kafkaconvergence.entity.basexml.notice.NoticeXml;
import fr.abes.kafkaconvergence.exception.IllegalPpnException;
import fr.abes.kafkaconvergence.repository.basexml.NoticesBibioRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

@Service
public class NoticeService {
    private final NoticesBibioRepository noticesBibioRepository;

    private final XmlMapper xmlMapper;

    public NoticeService(NoticesBibioRepository noticesBibioRepository, XmlMapper xmlMapper) {
        this.noticesBibioRepository = noticesBibioRepository;
        this.xmlMapper = xmlMapper;
    }

    public NoticeXml getNoticeByPpn(String ppn) throws IllegalPpnException, IOException {
        if (ppn == null)
            throw new IllegalPpnException("Le PPN ne peut pas être null");
        Optional<NoticesBibio> noticeOpt = this.noticesBibioRepository.findByPpn(ppn);
        if (noticeOpt.isPresent()) {
            try {
                return xmlMapper.readValue(noticeOpt.get().getDataXml().getCharacterStream(), NoticeXml.class);
            } catch (SQLException ex) {
                throw new IOException(ex);
            }
        }
        return null;
    }
}
