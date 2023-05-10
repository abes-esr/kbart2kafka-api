package fr.abes.kafkaconvergence.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@ApiModel(description = "Lines extracted from a kbart file")
public class LigneKbartDto {
    @ApiModelProperty("Title of the publication")
    private String publication_title;
    @ApiModelProperty("ISBN number")
    private String print_identifier;
    @ApiModelProperty("ISSN number")
    private String online_identifier;
    private String date_first_issue_online;
    private Integer num_first_vol_online;
    private Integer num_first_issue_online;
    private String date_last_issue_online;
    private Integer num_last_vol_online;
    private Integer num_last_issue_online;
    private String title_url;
    @ApiModelProperty("First author")
    private String first_author;
    private String title_id;
    private String embargo_info;
    private String coverage_depth;
    private String notes;
    private String publisher_name;
    @ApiModelProperty("Publication type")
    private String publication_type;
    private String date_monograph_published_print;
    @ApiModelProperty("Date of publication of the online monograph")
    private String date_monograph_published_online;
    private Integer monograph_volume;
    private String monograph_edition;
    @ApiModelProperty("First editor")
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
        return "publication title : " + this.publication_title + " / publication_type : " + this.publication_type +
                (this.online_identifier.isEmpty() ? "" : " / online_identifier : " + this.online_identifier) +
                (this.print_identifier.isEmpty() ? "" : " / print_identifier : " + this.print_identifier);
    }

    @JsonIgnore
    public String getAuthor() {
        return (!this.first_author.isEmpty()) ? this.first_author : this.first_editor;
    }

    @JsonIgnore
    public boolean isBestPpnEmpty() {
        return this.bestPpn == null || this.bestPpn.isEmpty();
    }

}
