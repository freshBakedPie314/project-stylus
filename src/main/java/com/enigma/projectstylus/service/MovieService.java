package com.enigma.projectstylus.service;

import com.enigma.projectstylus.dto.MovieResponse;
import com.enigma.projectstylus.dto.TDBDiscoverResponse;
import com.enigma.projectstylus.dto.TDBMovieResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

@Service
public class MovieService {

    @Autowired
    private RestClient restClient;

//    public ResponseEntity<List<MovieResponse>> getMovies() {
//        Random rand = new Random();
//
//        // Movies
//        CompletableFuture<TDBDiscoverResponse> movieFetch = CompletableFuture.supplyAsync(() ->
//                restClient.get()
//                        .uri(uriBuilder -> uriBuilder
//                                .path("/discover/movie")
//                                .queryParam("include_adult", "false")
//                                .queryParam("language", "en-US")
//                                .queryParam("sort_by", "vote_count.desc")
//                                .queryParam("vote_count.gte", 10000)
//                                .queryParam("page", rand.nextInt(8) + 1)
//                                .build())
//                        .retrieve()
//                        .body(TDBDiscoverResponse.class)
//        );
//
//        // TV shows
//        CompletableFuture<TDBDiscoverResponse> tvFetch = CompletableFuture.supplyAsync(() ->
//                restClient.get()
//                        .uri(uriBuilder -> uriBuilder
//                                .path("/discover/tv")
//                                .queryParam("include_adult", "false")
//                                .queryParam("language", "en-US")
//                                .queryParam("sort_by", "vote_count.desc")
//                                .queryParam("vote_count.gte", 5000) // TV shows naturally get fewer total votes than movies
//                                .queryParam("with_original_language", "en|ja") // 'ja' pulls top Anime, 'en' pulls top US shows
//                                .queryParam("page", rand.nextInt(3) + 1) // Top 120 most popular global shows
//                                .build())
//                        .retrieve()
//                        .body(TDBDiscoverResponse.class)
//        );
//
//        // Wait for both network calls to finish running in parallel
//        CompletableFuture.allOf(movieFetch, tvFetch).join();
//
//        List<MovieResponse> combinedPool = new ArrayList<>();
//        String imageBaseUrl = "https://image.tmdb.org/t/p/w500";
//
//        // Process Movie results
//        TDBDiscoverResponse movieResponse = movieFetch.join();
//        if (movieResponse != null && movieResponse.getResults() != null) {
//            for (TDBMovieResponse m : movieResponse.getResults()) {
//                combinedPool.add(MovieResponse.builder()
//                        .name(m.getTitle())
//                        .description(m.getOverview())
//                        .movieUrl(m.getPosterPath() != null ? imageBaseUrl + m.getPosterPath() : null)
//                        .build());
//            }
//        }
//
//        // Process TV/Anime results
//        TDBDiscoverResponse tvResponse = tvFetch.join();
//        if (tvResponse != null && tvResponse.getResults() != null) {
//            for (TDBMovieResponse t : tvResponse.getResults()) {
//                combinedPool.add(MovieResponse.builder()
//                        .name(t.getName())
//                        .description(t.getOverview())
//                        .movieUrl(t.getPosterPath() != null ? imageBaseUrl + t.getPosterPath() : null)
//                        .build());
//            }
//        }
//
//        if (combinedPool.isEmpty()) {
//            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
//        }
//
//        //Shuffle
//        Collections.shuffle(combinedPool);
//
//        // Take 5
//        int countToTake = Math.min(combinedPool.size(), 5);
//        List<MovieResponse> finalSelection = combinedPool.subList(0, countToTake);
//
//        return new ResponseEntity<>(finalSelection, HttpStatus.OK);
//    }

    @Cacheable(value = "movieSearches", key = "#query", unless = "#result == null")
    public List<MovieResponse> serachMoviesAndShows(String query) {
        // Cache miss

        //Get movies and shows and people
        TDBDiscoverResponse result = restClient.get().uri(uri -> uri
                        .path("/search/multi")
                        .queryParam("include_adult", "false")
                        .queryParam("language", "en-US")
                        .queryParam("query", query)
                        .build())
                .retrieve()
                .body(TDBDiscoverResponse.class);

        // Convert
        List<MovieResponse> movieResponseList = new ArrayList<>();

        if(result == null || result.getResults().isEmpty()) {
            return movieResponseList;
        }

        String imageBaseUrl = "https://image.tmdb.org/t/p/w200"; // Smaller resolution

        for(TDBMovieResponse item : result.getResults()) {
            if("person".equals(item.getMediaType())) continue;

            String name = item.getName() != null ? item.getName() : item.getTitle();

            if(name != null)
            {
                movieResponseList.add(MovieResponse.builder()
                        .name(name)
                        .description(item.getOverview())
                        .movieUrl(item.getPosterPath() != null ? imageBaseUrl + item.getPosterPath() : null)
                                .id(item.getId())
                        .build());
            }
        }
        return movieResponseList.subList(0, Math.min(movieResponseList.size(), 8));
    }
}