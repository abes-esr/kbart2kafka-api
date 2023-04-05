package fr.abes.kafkaconvergence.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ApiModel(description = "Response from the Sudoc-Api web service")
public class ResultWsSudocDto {
    @ApiModelProperty("List of ppns")
    private List<PpnWithTypeDto> ppns = new ArrayList<>();
    @ApiModelProperty("List of errors")
    private List<String> erreurs = new ArrayList<>();
}
