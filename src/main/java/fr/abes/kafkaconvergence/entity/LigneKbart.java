package fr.abes.kafkaconvergence.entity;


import fr.abes.kafkaconvergence.utils.PUBLICATION_TYPE;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "LIGNE_KBART")
@Getter
@Setter
@NoArgsConstructor
public class LigneKbart implements Serializable {
    @Id
    @Column(name = "IDT_LIGNE_KBART")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "LIGNE_KBART_SEQ")
    @SequenceGenerator(name = "LIGNE_KBART_SEQ", sequenceName = "LIGNE_KBART_SEQ")
    private Integer id;

    @Column(name = "PUBLICATION_TITLE", length = 4000, nullable = false)
    private String publicationTitle;
    @Column(name = "PRINT_IDENTIFIER")
    private String printIdentifier;
    @Column(name = "ONLINE_IDENTIFIER")
    private String onlineIdentifier;
    @Column(name = "DATE_FIRST_ISSUE_ONLINE")
    private String dateFirstIssueOnline;
    @Column(name = "NUM_FIRST_VOL_ONLINE")
    private String numFirstVolOnline;
    @Column(name = "NUM_FIRST_ISSUE_ONLINE")
    private String numFirstIssueOnline;
    @Column(name = "DATE_LAST_ISSUE_ONLINE")
    private String dateLastIssueOnline;
    @Column(name = "NUM_LAST_VOL_ONLINE")
    private String numLastVolOnline;
    @Column(name = "NUM_LAST_ISSUE_ONLINE")
    private String numLastIssueOnline;
    @Column(name = "TITLE_URL", length = 1000)
    private String titleUrl;
    @Column(name = "FIRST_AUTHOR")
    private String firstAuthor;
    @Column(name = "TITLE_ID")
    private String titleId;
    @Column(name = "EMBARGO_ID")
    private String embargoInfo;
    @Column(name = "COVERAGE_DEPTH")
    private String coverageDepth;
    @Column(name = "NOTES", length = 1000)
    private String notes;
    @Column(name = "PUBLISHER_NAME")
    private String publisherName;
    @Column(name = "PUBLICATION_TYPE")
    @Enumerated(EnumType.STRING)
    private PUBLICATION_TYPE publicationType;
    @Column(name = "DATE_MONOGRAPH_PUBLISHED_PRINT")
    private String dateMonographPublishedPrint;
    @Column(name = "DATE_MONOGRAPH_PUBLISHED_ONLIN")
    private String dateMonographPublishedOnlin;
    @Column(name = "MONOGRAPH_VOLUME")
    private String monographVolume;
    @Column(name = "MONOGRAPH_EDITION")
    private String monographEdition;
    @Column(name = "FIRST_EDITOR")
    private String firstEditor;
    @Column(name = "PARENT_PUBLICATION_TITLE_ID")
    private String parentPublicationTitleId;
    @Column(name = "PRECEDING_PUBLICATION_TITLE_ID")
    private String precedingPublicationTitleId;
    @Column(name = "ACCESS_TYPE")
    private String accessType;

    @Column(name = "NODASH_PRINT_ID")
    private String noDashPrintId;
    @Column(name = "NODASH_ONLINE_ID")
    private String noDashOnlineId;
    @Column(name = "PPN_FROM_ONLINEID")
    private String ppnFromOnlineId;
    @Column(name = "PPN_FROM_PRINTID")
    private String ppnFromPrintId;

    public LigneKbart(String publicationTitle) {
        this.publicationTitle = publicationTitle;
    }
}
