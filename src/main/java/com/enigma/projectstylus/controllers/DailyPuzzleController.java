package com.enigma.projectstylus.controllers;

import com.enigma.projectstylus.dto.daily.DailyPuzzleDTO;
import com.enigma.projectstylus.model.DailyPuzzles;
import com.enigma.projectstylus.repositories.DailyPuzzleRepository;
import com.enigma.projectstylus.service.daily.DailyPuzzleBatchGenerator;
import com.enigma.projectstylus.service.daily.DailyPuzzleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/daily")
public class DailyPuzzleController {

    @Autowired
    private DailyPuzzleBatchGenerator dailyPuzzleBatchGenerator;
    @Autowired
    private DailyPuzzleService dailyPuzzleService;


    @GetMapping("/generate")
    public ResponseEntity<List<DailyPuzzles>> generatePuzzleBatch()
    {
        List<DailyPuzzles> movies = dailyPuzzleBatchGenerator.generatePuzzleBatch(true);
        return ResponseEntity.ok(movies);
    }

    @GetMapping
    public ResponseEntity<DailyPuzzleDTO> getDailyPuzzle()
    {
        DailyPuzzleDTO puzzle =  dailyPuzzleService.getPuzzle();
        return ResponseEntity.ok(puzzle);
    }
}
