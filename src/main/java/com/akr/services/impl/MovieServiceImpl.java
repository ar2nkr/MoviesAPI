package com.akr.services.impl;

import com.akr.dtos.MovieDto;
import com.akr.entities.Movie;
import com.akr.repos.MovieRepo;
import com.akr.services.FileService;
import com.akr.services.MovieService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class MovieServiceImpl implements MovieService {

    private final MovieRepo movieRepo;
    private final FileService fileService;
    private final ModelMapper modelMapper;

    @Value("${project.poster}")
    private String path;

    @Value("${base.url}")
    private String baseUrl;

    public MovieServiceImpl(MovieRepo movieRepo, FileService fileService, ModelMapper modelMapper) {
        this.movieRepo = movieRepo;
        this.fileService = fileService;
        this.modelMapper = modelMapper;
    }


    @Override
    public MovieDto addMovie(MovieDto movieDto, MultipartFile file) throws IOException {

        if(Files.exists(Paths.get(path + File.separator + file.getOriginalFilename()))){
            throw new RuntimeException("File already exists, please enter another file name");
        }

        movieDto.setPoster(file.getOriginalFilename());
//        Movie movie = modelMapper.map(movieDto, Movie.class);
        Movie movie = new Movie(movieDto.getMovieId(), movieDto.getTitle(), movieDto.getDirector(), movieDto.getStudio(), movieDto.getMovieCast(),
                movieDto.getReleaseYear(), movieDto.getPoster());
        System.out.println(movie);
        Movie savedMovie = movieRepo.save(movie);

        String uploadedFileName = fileService.uploadFile(path, file);

        String posterUrl = baseUrl + "/file/" + uploadedFileName;

//        MovieDto newDto = modelMapper.map(savedMovie, MovieDto.class);
        MovieDto newDto = new MovieDto(savedMovie.getMovieId(),
                savedMovie.getTitle(), savedMovie.getDirector(), savedMovie.getStudio(), savedMovie.getMovieCast(), savedMovie.getReleaseYear(), savedMovie.getPoster(), posterUrl);
//        System.out.println(newDto);
        return newDto;
    }

    @Override
    public MovieDto getMovie(Integer movieId) {

        Movie savedMovie = movieRepo.findById(movieId).orElseThrow(() -> new RuntimeException("Movie Not Found"));

        String posterUrl = baseUrl + "/file/" + savedMovie.getPoster();

        MovieDto newDto = new MovieDto(savedMovie.getMovieId(),
                savedMovie.getTitle(), savedMovie.getDirector(), savedMovie.getStudio(), savedMovie.getMovieCast(), savedMovie.getReleaseYear(), savedMovie.getPoster(), posterUrl);
        System.out.println(newDto);
        return newDto;
    }

    @Override
    public List<MovieDto> getAllMovies() {

        List<Movie> allMovies = movieRepo.findAll();

        List<MovieDto> allMoviesDto = new ArrayList<>();

        for(Movie savedMovie: allMovies){
            String posterUrl = baseUrl + "/file/" + savedMovie.getPoster();
            allMoviesDto.add(new MovieDto(savedMovie.getMovieId(),
                    savedMovie.getTitle(), savedMovie.getDirector(), savedMovie.getStudio(), savedMovie.getMovieCast(), savedMovie.getReleaseYear(), savedMovie.getPoster(), posterUrl));
        }

        return allMoviesDto;
    }

    @Override
    public MovieDto updateMovie(Integer movieId, MovieDto movieDto, MultipartFile file) throws IOException {

        Movie savedMovie = movieRepo.findById(movieId).orElseThrow(() -> new RuntimeException("Movie Not Found"));

        String savedFileName = savedMovie.getPoster();

        if(file != null){
            boolean d = Files.deleteIfExists(Paths.get(path + File.separator + savedFileName));
            System.out.println(d);
            savedFileName = fileService.uploadFile(path, file);
            movieDto.setPoster(savedFileName);
        }

        Movie updatedMovie = new Movie(movieId, movieDto.getTitle(), movieDto.getDirector(), movieDto.getStudio(), movieDto.getMovieCast(),
                movieDto.getReleaseYear(), movieDto.getPoster());

        Movie savedUpdatedMovie = movieRepo.save(updatedMovie);

        String posterUrl = baseUrl + "/file/" + savedFileName;

//        MovieDto newDto = modelMapper.map(updatedMovie, MovieDto.class);
        MovieDto newDto = new MovieDto(savedUpdatedMovie.getMovieId(),
                savedUpdatedMovie.getTitle(), savedUpdatedMovie.getDirector(), savedUpdatedMovie.getStudio(), savedUpdatedMovie.getMovieCast(), savedUpdatedMovie.getReleaseYear(), savedUpdatedMovie.getPoster(), posterUrl);

        newDto.setPosterUrl(posterUrl);

        return newDto;

    }

    @Override
    public String deleteMovie(Integer movieId) throws IOException {
        Movie savedMovie = movieRepo.findById(movieId).orElseThrow(() -> new RuntimeException("Movie Not Found with id "+ movieId));
        Files.deleteIfExists(Paths.get(path + File.separator + savedMovie.getPoster()));
        movieRepo.deleteById(movieId);
        return "Movie successfully deleted with id " + movieId;
    }
}
