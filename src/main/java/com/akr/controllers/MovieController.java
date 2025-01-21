package com.akr.controllers;

import com.akr.dtos.MovieDto;
import com.akr.exceptions.EmptyFileException;
import com.akr.services.MovieService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/movie")
public class MovieController {

    private final MovieService movieService;


    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    // since there is multipart file in req, we need to use @RequestPart for all
    @PostMapping("/")
    public ResponseEntity<MovieDto> addMovieHandler(@RequestPart MultipartFile file, @RequestPart @Validated String movieDto) throws IOException {

        if(file.isEmpty()){
            throw new EmptyFileException("File cannot be empty");
        }
        MovieDto dto = convertToMovieDto(movieDto);
        System.out.println(dto);
        return new ResponseEntity<>(movieService.addMovie(dto, file), HttpStatus.CREATED);
    }

    // converting string movie to dto json
    private MovieDto convertToMovieDto(String movieDtoObj) throws JsonProcessingException {
        return new ObjectMapper().readValue(movieDtoObj, MovieDto.class);
    }

    @GetMapping("/{movieId}")
    public ResponseEntity<MovieDto> getMovieHandler(@PathVariable Integer movieId){
        return new ResponseEntity<>(movieService.getMovie(movieId), HttpStatus.OK);
    }

    @GetMapping("/all")
    public ResponseEntity<List<MovieDto>> getAllMoviesHandler(){
        return new ResponseEntity<>(movieService.getAllMovies(), HttpStatus.OK);
    }

    @PutMapping("/{movieId}")
    public ResponseEntity<MovieDto> updateMovieHandler(@PathVariable Integer movieId, @RequestPart String movieDto,@RequestPart MultipartFile file) throws IOException {
        if(file.isEmpty()) file = null;
        MovieDto dto = convertToMovieDto(movieDto);
        return ResponseEntity.ok(movieService.updateMovie(movieId, dto, file));
    }

    @DeleteMapping("/{movieId}")
    public ResponseEntity<String> deleteMovieHandler(@PathVariable Integer movieId) throws IOException {
        return ResponseEntity.ok(movieService.deleteMovie(movieId));
    }
}
