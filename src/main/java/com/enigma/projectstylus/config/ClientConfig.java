package com.enigma.projectstylus.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ClientConfig {
    @Value("${tmdb.readKey}")
    private String tmdbReadKey;

    @Bean
    public RestClient restTemplate() {
        System.out.println("tmdbReadKey: " + tmdbReadKey);
        return RestClient.builder()
                .baseUrl("https://api.themoviedb.org/3/discover/movie?include_adult=false&include_video=false&language=en-US&page=1&sort_by=popularity.desc")
                .defaultHeader("Authorization", "Bearer " + tmdbReadKey)
                .defaultHeader("Accept", "application/json")
                .build();
    }
}

