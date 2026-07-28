package com.enigma.projectstylus.controllers;

import com.enigma.projectstylus.dto.TDBDiscoverResponse;
import com.enigma.projectstylus.service.MovieService;
import lombok.Getter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MovieController {

    private final MovieService movieService;
    MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping("/movies")
    public ResponseEntity<TDBDiscoverResponse> get()
    {
        return movieService.getMovies();
    }

    @GetMapping("/hi")
    public ResponseEntity<String> hello()
    {
        return ResponseEntity.ok("hello");
    }
}
