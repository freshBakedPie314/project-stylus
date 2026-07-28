package com.enigma.projectstylus.service;

import com.enigma.projectstylus.dto.TDBDiscoverResponse;
import com.enigma.projectstylus.dto.TDBMovieResponse;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@Service
public class MovieService {

    @Autowired
    private RestClient restClient;

    public ResponseEntity<TDBDiscoverResponse> getMovies()
    {
        TDBDiscoverResponse response = restClient.get().retrieve().body(TDBDiscoverResponse.class);
        return new ResponseEntity<TDBDiscoverResponse>(response, HttpStatus.OK);
    }
}
