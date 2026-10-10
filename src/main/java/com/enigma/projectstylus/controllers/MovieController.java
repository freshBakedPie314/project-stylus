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

    @GetMapping("/search/{page}") // Corrected spelling from /serach to /search
    public ResponseEntity<List<MovieResponse>> get(@RequestParam String query,  @PathVariable int page) {
        List<MovieResponse> movies = movieService.serachMoviesAndShows(query, page);
        return ResponseEntity.ok(movies);
    }

    @GetMapping("/hi")
    public ResponseEntity<String> hello() {
        return ResponseEntity.ok("hello");
    }
}