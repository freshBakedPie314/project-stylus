package com.enigma.projectstylus.service.daily;

import com.enigma.projectstylus.dto.daily.DailyPuzzleDTO;
import com.enigma.projectstylus.model.DailyPuzzles;
import com.enigma.projectstylus.repositories.DailyPuzzleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class DailyPuzzleService {

    @Autowired
    private DailyPuzzleRepository  dailyPuzzleRepository;

    @Autowired
    private RedisTemplate<String, DailyPuzzleDTO> redisTemplate;

    private final String prompt = "You are an expert game designer creating clues for a daily movie-guessing puzzle game.\n" +
            "\n" +
            "Given the movie title and overview below, generate exactly 5 distinct clues that help players guess the movie one step at a time. Follow the strict difficulty progression rules:\n" +
            "\n" +
            "### Clue Difficulty Progression:\n" +
            "- Clue 1 (Hard / Hilariously Poorly Described): A comedic, intentionally misleading, or \"technically true but absurd\" one-sentence summary of the plot or main character's situation. (e.g., For Finding Nemo: \"An overprotective single dad crosses an ocean because a dentist kidnapped his disabled son.\")\n" +
            "- Clue 2 (Medium-Hard / The Setting & Central Conflict): Highlights the unique world, workplace, time period, or central dilemma without naming characters or explicit plot twists.\n" +
            "- Clue 3 (Medium / The Key Mechanic or Trope): Focuses on an iconic prop, recurring visual motif, famous mechanic, or distinct character dynamic (e.g., spinning top, red dress, tape recorder, strict mentor).\n" +
            "- Clue 4 (Medium-Easy / Famous Dialogue or Iconic Scene): Describes a legendary scene, a subtly masked quote, or the critical turning point that film fans will recognize.\n" +
            "- Clue 5 (Easy / The Dead Giveaway): Mentions the director, genre, release era, and unmistakable core premise—virtually giving it away without explicitly stating the title or lead actor's full name.\n" +
            "\n" +
            "### Strict Formatting Rules:\n" +
            "1. Never mention the movie's title anywhere in the clues.\n" +
            "2. Return ONLY a valid JSON object matching the schema below. No markdown backticks, no explanations.\n" +
            "\n" +
            "### JSON Output Schema:\n" +
            "{\n" +
            "  \"clues\": [\n" +
            "    {\"order\": 1, \"text\": \"Clue 1 text here\"},\n" +
            "    {\"order\": 2, \"text\": \"Clue 2 text here\"},\n" +
            "    {\"order\": 3, \"text\": \"Clue 3 text here\"},\n" +
            "    {\"order\": 4, \"text\": \"Clue 4 text here\"},\n" +
            "    {\"order\": 5, \"text\": \"Clue 5 text here\"}\n" +
            "  ]\n" +
            "}\n" +
            "\n" +
            "### Input:\n" +
            "Movie Title: {{movieTitle}}\n" +
            "Overview: {{movieOverview}}";


    @Autowired
    private RestClient restClient;

    public DailyPuzzleDTO getPuzzle()
    {
        LocalDate today = LocalDate.now();
        DailyPuzzleDTO puzzle = redisTemplate.opsForValue().get(today.toString());
        if(puzzle == null) {
            puzzle = cacheTodaysPuzzle();
        }

        return puzzle;
    }

    @Scheduled(cron = "0 30 23 * * *", zone = "Asia/Kolkata")
    public void scheduleNextPuzzle()
    {
        LocalDate targetDate = LocalDate.now().plusDays(1);

        DailyPuzzles puzzleMovieId = dailyPuzzleRepository.findByDate(targetDate);

        String uri = "https://api.themoviedb.org/3/movie/" + puzzleMovieId.getMovieId().toString() + "?append_to_response=credits&language=en-US";

        DailyPuzzleDTO fullMovieDetails = restClient.get()
                .uri(uri)
                .retrieve()
                .body(DailyPuzzleDTO.class);

        redisTemplate.opsForValue().set(targetDate.toString(), fullMovieDetails, Duration.ofHours(25));
    }

    public DailyPuzzleDTO cacheTodaysPuzzle()
    {
        LocalDate targetDate = LocalDate.now();

        DailyPuzzles puzzleMovieId = dailyPuzzleRepository.findByDate(targetDate);

        String uri = "https://api.themoviedb.org/3/movie/" + puzzleMovieId.getMovieId().toString() + "?append_to_response=credits&language=en-US";

        DailyPuzzleDTO fullMovieDetails = restClient.get()
                .uri(uri)
                .retrieve()
                .body(DailyPuzzleDTO.class);

        redisTemplate.opsForValue().set(targetDate.toString(), fullMovieDetails, Duration.ofHours(25));
        return fullMovieDetails;
    }
}
