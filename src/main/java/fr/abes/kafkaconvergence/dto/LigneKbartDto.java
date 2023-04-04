package fr.abes.kafkaconvergence.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LigneKbartDto {
    private String publication_title;
    private String print_identifier;
    private String online_identifier;
    private String date_first_issue_online;
    private Integer num_first_vol_online;
    private Integer num_first_issue_online;
    private String date_last_issue_online;
    private Integer num_last_vol_online;
    private Integer num_last_issue_online;
    private String title_url;
    private String first_author;
    private String title_id;
    private String embargo_info;
    private String coverage_depth;
    private String notes;
    private String publisher_name;
    private String publication_type;
    private String date_monograph_published_print;
    private String date_monograph_published_online;
    private Integer monograph_volume;
    private String monograph_edition;
    private String first_editor;
    private String parent_publication_title_id;
    private String preceding_publication_title_id;
    private String access_type;
    private String bestPpn;

    @Override
    public int hashCode() {
        return this.publication_title.hashCode() * this.online_identifier.hashCode() * this.print_identifier.hashCode();
    }

    @Override
    public String toString() {
        return this.publication_title;
    }

    public String getAuthor() {
        return (!this.first_author.isEmpty()) ? this.first_author : this.first_editor;
    }
}
