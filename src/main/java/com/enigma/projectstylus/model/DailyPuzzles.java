package com.enigma.projectstylus.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "daily_puzzles")
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyPuzzles {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "movie_id")
    private Long movieId;

    private String title;

    @Column(name = "puzzle_date")
    private LocalDate date;
}
