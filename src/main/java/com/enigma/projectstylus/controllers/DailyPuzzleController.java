package com.enigma.projectstylus.controllers;

import com.enigma.projectstylus.dto.daily.AiClueResponseDTO;
import com.enigma.projectstylus.dto.daily.DailyClueDTO;
import com.enigma.projectstylus.dto.daily.DailyPuzzleDTO;
import com.enigma.projectstylus.dto.daily.DailyPuzzleTmdbResponseDTO;
import com.enigma.projectstylus.dto.daily.guess.DailyGuessRequestDTO;
import com.enigma.projectstylus.dto.daily.guess.DailyGuessResponseDTO;
import com.enigma.projectstylus.model.DailyPuzzles;
import com.enigma.projectstylus.service.daily.DailyPuzzleBatchGenerator;
import com.enigma.projectstylus.service.daily.DailyPuzzleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/clue/{index}")
    public ResponseEntity<DailyClueDTO> getDailyPuzzle(@PathVariable String index)
    {
        DailyClueDTO puzzle =  dailyPuzzleService.getClueOrder(Integer.parseInt(index));
        return ResponseEntity.ok(puzzle);
    }

    @GetMapping("/ai")
    public ResponseEntity<AiClueResponseDTO> getDailyPuzzle(@RequestParam String name, @RequestParam String desc)
    {
        AiClueResponseDTO puzzle =  dailyPuzzleService.getClue(name, desc);
        return ResponseEntity.ok(puzzle);
    }

    @PostMapping("/guess")
    public ResponseEntity<DailyGuessResponseDTO> submitGuess(@RequestBody DailyGuessRequestDTO request) {
        return ResponseEntity.ok(dailyPuzzleService.checkGuess(request));
    }
}
