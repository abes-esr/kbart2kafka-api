package fr.abes.kafkaconvergence.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.abes.kafkaconvergence.dto.ErreurResultDto;
import fr.abes.kafkaconvergence.dto.LigneKbartDto;
import fr.abes.kafkaconvergence.entity.ErreurResult;
import fr.abes.kafkaconvergence.entity.LigneKbart;
import lombok.RequiredArgsConstructor;
import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;

@Component
@RequiredArgsConstructor
public class DtoMapper {
    private final UtilsMapper mapper;

    private final ObjectMapper objectMapper;

    @Bean
    public void converterLigneKbartDtoToLigneKbart() {
        Converter<LigneKbartDto, LigneKbart> myConverter = new Converter<LigneKbartDto, LigneKbart>() {
            @Override
            public LigneKbart convert(MappingContext<LigneKbartDto, LigneKbart> context) {
                LigneKbartDto source = context.getSource();
                LigneKbart ligneKbart = new LigneKbart(source.getPublication_title());

                ligneKbart.setPrintIdentifier(source.getPrint_identifier());
                ligneKbart.setOnlineIdentifier(source.getOnline_identifier());
                ligneKbart.setDateFirstIssueOnline(source.getDate_first_issue_online());
                ligneKbart.setNumFirstIssueOnline(String.valueOf(source.getNum_first_issue_online()));
                ligneKbart.setDateLastIssueOnline(source.getDate_last_issue_online());
                ligneKbart.setNumLastVolOnline(String.valueOf(source.getNum_last_vol_online()));
                ligneKbart.setNumLastIssueOnline(String.valueOf(source.getNum_last_issue_online()));
                ligneKbart.setTitleUrl(source.getTitle_url());
                ligneKbart.setFirstAuthor(source.getFirst_author());
                ligneKbart.setTitleId(source.getTitle_id());
                ligneKbart.setEmbargoInfo(source.getEmbargo_info());
                ligneKbart.setCoverageDepth(source.getCoverage_depth());
                ligneKbart.setNotes(source.getNotes());
                ligneKbart.setPublisherName(source.getPublisher_name());
                ligneKbart.setPublicationType(Enum.valueOf(PUBLICATION_TYPE.class, source.getPublication_type()));
                ligneKbart.setDateMonographPublishedPrint(source.getDate_monograph_published_print());
                ligneKbart.setDateMonographPublishedOnlin(source.getDate_monograph_published_online());
                ligneKbart.setMonographVolume(String.valueOf(source.getMonograph_volume()));
                ligneKbart.setMonographEdition(source.getMonograph_edition());
                ligneKbart.setFirstEditor(source.getFirst_editor());
                ligneKbart.setParentPublicationTitleId(source.getParent_publication_title_id());
                ligneKbart.setPrecedingPublicationTitleId(source.getPreceding_publication_title_id());
                ligneKbart.setAccessType(source.getAccess_type());
                ligneKbart.setNoDashOnlineId(source.getOnline_identifier().replace("-", ""));
                ligneKbart.setNoDashPrintId(source.getPrint_identifier().replace("-", ""));
                ligneKbart.setPpnFromOnlineId(source.getBestPpn());

                return ligneKbart;
            }
        };
        mapper.addConverter(myConverter);
    }

    @Bean
    public void converterErreurResultDtoToErreurResult() {
        Converter<ErreurResultDto, ErreurResult> myConverter = new Converter<ErreurResultDto, ErreurResult>() {
            @Override
            public ErreurResult convert(MappingContext<ErreurResultDto, ErreurResult> context) {
                ErreurResultDto source = context.getSource();
                ErreurResult erreurResult = new ErreurResult();
                try {
                    erreurResult.setLigneKbart(objectMapper.writeValueAsString(source.getLigneKbartDto()));
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
                erreurResult.setMessages(source.getMessages());
                erreurResult.setPpns(source.getPpns());

                return erreurResult;
            }
        };
        mapper.addConverter(myConverter);
    }
}
