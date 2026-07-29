package com.enigma.projectstylus.controllers;

import com.enigma.projectstylus.dto.MovieResponse;
import com.enigma.projectstylus.service.MovieService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/movies")
public class MovieController {

    private final MovieService movieService;

    MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping("/search") // Corrected spelling from /serach to /search
    public ResponseEntity<List<MovieResponse>> get(@RequestParam String query) {
        if (query == null || query.trim().length() <= 2) {
            return ResponseEntity.badRequest().build();
        }

        List<MovieResponse> movies = movieService.serachMoviesAndShows(query);
        return ResponseEntity.ok(movies);
    }

    @GetMapping("/hi")
    public ResponseEntity<String> hello() {
        return ResponseEntity.ok("hello");
    }
}