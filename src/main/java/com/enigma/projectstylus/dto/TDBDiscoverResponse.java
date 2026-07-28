package com.enigma.projectstylus.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TDBDiscoverResponse {
    @JsonProperty("page")
    private Long page;
    @JsonProperty("results")
    private List<TDBMovieResponse> results;
    @JsonProperty("total_pages")
    private Long totalPages;
    @JsonProperty("total_results")
    private Long totalResults;
}
