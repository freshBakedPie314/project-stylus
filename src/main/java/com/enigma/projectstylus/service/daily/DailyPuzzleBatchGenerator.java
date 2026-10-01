package com.enigma.projectstylus.service.daily;

import com.enigma.projectstylus.dto.TDBDiscoverResponse;
import com.enigma.projectstylus.dto.TDBMovieResponse;
import com.enigma.projectstylus.model.DailyPuzzles;
import com.enigma.projectstylus.repositories.DailyPuzzleRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class DailyPuzzleBatchGenerator {

    @Autowired
    private RestClient restClient;
    @Autowired
    private DailyPuzzleRepository dailyPuzzleRepository;

    public List<DailyPuzzles> generatePuzzleBatch(boolean thisYear) {
        LocalDateTime now = LocalDateTime.now();

        int targetYear =  now.getYear();
        if(!thisYear){
            targetYear = targetYear + 1;
        }

        LocalDate startDate = LocalDate.of(targetYear,1,1);

        int daysCount = startDate.lengthOfYear();
        String topMoviesUrl = "https://api.themoviedb.org/3/discover/movie?include_adult=false&include_video=false&language=en-US&sort_by=vote_count.desc&without_genres=99,10755&vote_count.gte=5000";
        int baseYear = 2026;
        int yearOffset = Math.floorMod(targetYear - baseYear, 2);
        int startPage = (yearOffset * 20) + 1;

        Set<Long> seenIds = new HashSet<>();

        List<DailyPuzzles> dailyMovieTmdbDTOList = new ArrayList<>();

        int page = startPage;
        while(dailyMovieTmdbDTOList.size() < daysCount){
            String uri = topMoviesUrl + "&page=" + page;
            TDBDiscoverResponse topMovies = restClient.get().uri(uri)
                    .retrieve()
                    .body(TDBDiscoverResponse.class);

            for(TDBMovieResponse item : topMovies.getResults())
            {
                if(seenIds.contains(item.getId())) continue;
                seenIds.add(item.getId());

                DailyPuzzles newMovie = DailyPuzzles.builder().movieId(item.getId())
                        .title(item.getTitle())
                        .build();

                dailyMovieTmdbDTOList.add(newMovie);
            }
            page++;
        }

        System.out.println(dailyMovieTmdbDTOList);


        Collections.shuffle(dailyMovieTmdbDTOList);

        LocalDate currentDate = startDate;
        for(DailyPuzzles item : dailyMovieTmdbDTOList){
            if(currentDate.getDayOfWeek() == DayOfWeek.SATURDAY)
            {
                currentDate = currentDate.plusDays(1);
            }
            item.setDate(currentDate);
            currentDate = currentDate.plusDays(1);
        }
        savePuzzleBatch(dailyMovieTmdbDTOList);
        return dailyMovieTmdbDTOList;
    }

    public List<DailyPuzzles> puzzleAlgoTest(boolean thisYear) {
        LocalDateTime now = LocalDateTime.now();

        int targetYear =  now.getYear();
        if(!thisYear){
            targetYear = targetYear + 1;
        }

        LocalDate startDate = LocalDate.of(targetYear,1,1);

        int daysCount = startDate.lengthOfYear();
        String topMoviesUrl = "https://api.themoviedb.org/3/discover/movie?include_adult=false&include_video=false&language=en-US&sort_by=vote_count.desc&without_genres=99,10755&vote_count.gte=3000";
        int baseYear = 2026;
        int yearOffset = Math.floorMod(targetYear - baseYear, 4);
        int startPage = (yearOffset * 20) + 1;

        Set<Long> seenIds = new HashSet<>();

        List<DailyPuzzles> dailyMovieTmdbDTOList = new ArrayList<>();

        int page = startPage;
        while(dailyMovieTmdbDTOList.size() < daysCount){
            String uri = topMoviesUrl + "&page=" + page;
            TDBDiscoverResponse topMovies = restClient.get().uri(uri)
                    .retrieve()
                    .body(TDBDiscoverResponse.class);

            for(TDBMovieResponse item : topMovies.getResults())
            {
                if(seenIds.contains(item.getId())) continue;
                seenIds.add(item.getId());

                DailyPuzzles newMovie = DailyPuzzles.builder().movieId(item.getId())
                        .title(item.getTitle())
                        .build();

                dailyMovieTmdbDTOList.add(newMovie);
            }
            page++;
        }

        System.out.println(dailyMovieTmdbDTOList);

        Collections.shuffle(dailyMovieTmdbDTOList);

        LocalDate currentDate = startDate;
        for(DailyPuzzles item : dailyMovieTmdbDTOList){
            if(currentDate.getDayOfWeek() == DayOfWeek.SATURDAY)
            {
                currentDate = currentDate.plusDays(1);
            }
            item.setDate(currentDate);
            currentDate = currentDate.plusDays(1);
        }
        savePuzzleBatch(dailyMovieTmdbDTOList);
        return  dailyMovieTmdbDTOList;
    }

    @Transactional
    public void savePuzzleBatch(List<DailyPuzzles> dailyMovieTmdbDTOList) {
        dailyPuzzleRepository.saveAll(dailyMovieTmdbDTOList);
    }

}
