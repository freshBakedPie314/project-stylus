package com.enigma.projectstylus.dto.daily;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DailyClueDTO {
    private Integer clueOrder;
    private String text;

    private List<String> genres;
    private String actor;
    private String posterBlurUrl;
}
