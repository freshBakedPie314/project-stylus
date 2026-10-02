package com.enigma.projectstylus.repositories;


import com.enigma.projectstylus.model.DailyPuzzles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface  DailyPuzzleRepository extends JpaRepository<DailyPuzzles, Long> {

    DailyPuzzles findByDate(LocalDate date);
}
