package com.enigma.projectstylus.service.daily;

import com.enigma.projectstylus.dto.daily.*;
import com.enigma.projectstylus.dto.daily.guess.DailyGuessRequestDTO;
import com.enigma.projectstylus.dto.daily.guess.DailyGuessResponseDTO;
import com.enigma.projectstylus.model.DailyPuzzles;
import com.enigma.projectstylus.repositories.DailyPuzzleRepository;
import com.enigma.projectstylus.service.blob.BlobStorageService;
import com.enigma.projectstylus.util.ImageBlurUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DailyPuzzleService {

    @Value("${gemini.api.key.daily}")
    private String apiKey;

    // gemini-2.5-flash
    private final String BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash-lite:generateContent";

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private RestClient tmdbRestClient;

    RestClient plainClient = RestClient.create();

    private final RestClient geminiRestClient = RestClient.builder().build();

    @Autowired
    private BlobStorageService blobStorageService;

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




    public DailyClueDTO getClueOrder(int order)
    {
        if (order < 1 || order > 5) {
            throw new IllegalArgumentException("Clue order must be between 1 and 5");
        }

        LocalDate today = LocalDate.now();
        DailyPuzzleDTO puzzle = redisTemplate.opsForValue().get(today.toString());
        if (puzzle == null) {
            puzzle = cacheTodaysPuzzle();
        }

        DailyClueDTO dto = new DailyClueDTO();
        dto.setClueOrder(order);

        if (puzzle.getClues() != null && puzzle.getClues().size() >= order) {
            dto.setText(puzzle.getClues().get(order - 1).getText());
        }

        if (puzzle.getBlurredPosterUrls() != null) {
            dto.setPosterBlurUrl(puzzle.getBlurredPosterUrls().get(order));
        }

        switch (order) {
            case 1 -> {
                if (puzzle.getGenres() != null)
                {
                    dto.setGenres(puzzle.getGenres().stream()
                            .map(genre -> genre.getName())
                            .toList());
                }
            }
            case 2 ->
            {

            }
            case 3 ->
            {
                if (hasCastIndex(puzzle, 2))
                {
                    dto.setActor(puzzle.getCredits().getCast().get(2).getName());
                }
            }
            case 4 -> {
                if (hasCastIndex(puzzle, 1))
                {
                    dto.setActor(puzzle.getCredits().getCast().get(1).getName());
                }
            }
            case 5 -> {
                if (hasCastIndex(puzzle, 0))
                {
                    dto.setActor(puzzle.getCredits().getCast().get(0).getName());
                }
            }
        }

        return dto;
    }

    private boolean hasCastIndex(DailyPuzzleDTO puzzle, int index)
    {
        return puzzle.getCredits() != null
                && puzzle.getCredits().getCast() != null
                && puzzle.getCredits().getCast().size() > index;
    }

    private static final ZoneId ZONE_KOLKATA = ZoneId.of("Asia/Kolkata");

    @Scheduled(cron = "0 30 23 * * *", zone = "Asia/Kolkata")
    public void scheduleNextPuzzle() {
        LocalDate tomorrow = LocalDate.now(ZONE_KOLKATA).plusDays(1);
        buildAndCachePuzzle(tomorrow);

        // Delete old images
        LocalDate twoDaysAgo = LocalDate.now(ZONE_KOLKATA).minusDays(2);
        List<String> oldKeys = List.of(
                "posters/" + twoDaysAgo + "/blur-1.jpg",
                "posters/" + twoDaysAgo + "/blur-2.jpg",
                "posters/" + twoDaysAgo + "/blur-3.jpg",
                "posters/" + twoDaysAgo + "/blur-4.jpg",
                "posters/" + twoDaysAgo + "/blur-5.jpg"
        );
        blobStorageService.deleteImages(oldKeys);
    }

    public DailyPuzzleDTO cacheTodaysPuzzle() {
        LocalDate today = LocalDate.now(ZONE_KOLKATA);
        return buildAndCachePuzzle(today);
    }

    private DailyPuzzleDTO buildAndCachePuzzle(LocalDate targetDate) {
        Long targetMovieId;

        if (targetDate.getDayOfWeek() == DayOfWeek.SATURDAY) {
            // Saturday Special: Trend dynamically from TMDB
            targetMovieId = fetchSaturdayTrendingMovieId();
        } else {
            // Normal days: Pull from DB schedule
            DailyPuzzles puzzleMovie = dailyPuzzleRepository.findByDate(targetDate);
            if (puzzleMovie == null) {
                throw new IllegalStateException("No daily puzzle movie configured in database for: " + targetDate);
            }
            targetMovieId = puzzleMovie.getMovieId();
        }

        // Proceed with fetching full details using targetMovieId
        String uri = "https://api.themoviedb.org/3/movie/" + targetMovieId + "?append_to_response=credits&language=en-US";

        DailyPuzzleTmdbResponseDTO fullMovieDetails = tmdbRestClient.get()
                .uri(uri)
                .retrieve()
                .body(DailyPuzzleTmdbResponseDTO.class);

        AiClueResponseDTO clues = getClue(fullMovieDetails.getTitle(), fullMovieDetails.getOverview());

        DailyPuzzleDTO dailyPuzzle = new DailyPuzzleDTO();
        dailyPuzzle.fromDailyPuzzleTmdbResponseDTO(fullMovieDetails);
        dailyPuzzle.setClues(clues.getClues());

        // --- BLURRED POSTER PIPELINE ---
        if (fullMovieDetails.getPosterPath() != null) {
            try {
                String posterUrl = "https://image.tmdb.org/t/p/w500" + fullMovieDetails.getPosterPath();
                byte[] rawPosterBytes = plainClient.get()
                        .uri(posterUrl)
                        .retrieve()
                        .body(byte[].class);

                Map<Integer, String> blurredMap = new HashMap<>();

                for (int order = 1; order <= 5; order++) {
                    int radius = ImageBlurUtil.getBlurRadiusForOrder(order);
                    byte[] blurredBytes = ImageBlurUtil.blurImageBytes(rawPosterBytes, radius);

                    String key = "posters/" + targetDate + "/blur-" + order + ".jpg";
                    String cdnUrl = blobStorageService.uploadImage(key, blurredBytes);
                    blurredMap.put(order, cdnUrl);
                }

                dailyPuzzle.setBlurredPosterUrls(blurredMap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        redisTemplate.opsForValue().set(targetDate.toString(), dailyPuzzle, Duration.ofHours(25));
        return dailyPuzzle;
    }

    public DailyGuessResponseDTO checkGuess(DailyGuessRequestDTO guess)
    {
        LocalDate today = LocalDate.now();
        DailyPuzzleDTO puzzle = redisTemplate.opsForValue().get(today.toString());
        if (puzzle == null) {
            puzzle = cacheTodaysPuzzle();
        }

        boolean isCorrect = guess.getMovieId().equals(puzzle.getId());
        int currentStep = guess.getCurrentClueOrder() != null ? guess.getCurrentClueOrder() : 1;

        // Correct
        if(isCorrect)
        {
            return  DailyGuessResponseDTO.builder()
                    .correct(true)
                    .gameOver(true)
                    .message("That's right")
                    .movieTitle(puzzle.getTitle())
                    .posterPath(puzzle.getFullPosterUrl())
                    .build();
        }

        //Incorrect Game Over
        if(currentStep >= 5)
        {
            return  DailyGuessResponseDTO.builder()
                    .correct(false)
                    .gameOver(true)
                    .message("Better luck tomorrow")
                    .movieTitle(puzzle.getTitle())
                    .posterPath(puzzle.getFullPosterUrl())
                    .build();
        }

        //Incorrect, guesses left
        int nextStep = currentStep + 1;
        DailyClueDTO nextClue = getClueOrder(nextStep);

        return DailyGuessResponseDTO.builder()
                .correct(false)
                .gameOver(false)
                .message("Incorrect guess. Here is your next clue!")
                .nextClue(nextClue)
                .build();
    }
    public AiClueResponseDTO getClue(String movieName, String description)
    {
        String payload = """
            {
              "contents": [{
                "parts": [{"text": "%s"}]
              }],
              "generationConfig": {
                "responseMimeType": "application/json",
                "responseSchema": {
                  "type": "OBJECT",
                  "properties": {
                    "clues": {
                      "type": "ARRAY",
                      "items": {
                        "type": "OBJECT",
                        "properties": {
                          "order": { "type": "INTEGER" },
                          "text": { "type": "STRING" }
                        },
                        "required": ["order", "text"]
                      }
                    }
                  },
                  "required": ["clues"]
                }
              }
            }""";

        String finalPrompt = prompt
                .replace("{{movieTitle}}", movieName)
                .replace("{{movieOverview}}", description);

        payload = payload.formatted(finalPrompt.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n"));

        String response = geminiRestClient.post()
                .uri(BASE_URL + "?key=" + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .body(String.class);

        String responseJson = objectMapper.readTree(response)
                .path("candidates").get(0)
                .path("content")
                .path("parts").get(0)
                .path("text")
                .asText();

        AiClueResponseDTO parsed =  objectMapper.readValue(responseJson, AiClueResponseDTO.class);
        return parsed;
    }

    private Long fetchSaturdayTrendingMovieId() {
        String url = "https://api.themoviedb.org/3/trending/movie/week?language=en-US";

        TmdbTrendingResponseDTO trendingResponse = tmdbRestClient.get()
                .uri(url)
                .retrieve()
                .body(TmdbTrendingResponseDTO.class);

        if (trendingResponse == null || trendingResponse.getResults() == null || trendingResponse.getResults().isEmpty()) {
            throw new IllegalStateException("Failed to fetch trending movies from TMDB for Saturday special");
        }

        // Pick the top trending movie that has a poster and at least 100 votes
        return trendingResponse.getResults().stream()
                .filter(m -> m.getPosterPath() != null && m.getVoteCount() != null && m.getVoteCount() >= 100)
                .map(TmdbTrendingResponseDTO.TrendingMovieItem::getId)
                .findFirst()
                .orElse(trendingResponse.getResults().get(0).getId());
    }
}
