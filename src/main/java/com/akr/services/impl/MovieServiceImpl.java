package com.akr.services.impl;

import com.akr.dtos.MovieDto;
import com.akr.dtos.MoviePageResponse;
import com.akr.entities.Movie;
import com.akr.exceptions.FileExistsException;
import com.akr.exceptions.MovieAlreadyExistsException;
import com.akr.exceptions.MovieNotFoundException;
import com.akr.repos.MovieRepo;
import com.akr.services.FileService;
import com.akr.services.MovieService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
            throw new FileExistsException("File already exists, please enter another file name");
        }
        Movie movieExists = movieRepo.findByTitleAndDirectorAndReleaseYear(movieDto.getTitle(), movieDto.getDirector(), movieDto.getReleaseYear());
        if(movieExists != null) throw new MovieAlreadyExistsException("Movie already present, please add a new movie");

        movieDto.setPoster(file.getOriginalFilename());
//        Movie movie = modelMapper.map(movieDto, Movie.class);
        Movie movie = new Movie(movieDto.getTitle(), movieDto.getDirector(), movieDto.getStudio(), movieDto.getMovieCast(),
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

        Movie savedMovie = movieRepo.findById(movieId).orElseThrow(() -> new MovieNotFoundException("Movie Not Found"));

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

        Movie savedMovie = movieRepo.findById(movieId).orElseThrow(() -> new MovieNotFoundException("Movie Not Found"));

        String savedFileName = savedMovie.getPoster();

        if(file != null){
            Files.deleteIfExists(Paths.get(path + File.separator + savedFileName));
//            System.out.println(d);
            savedFileName = fileService.uploadFile(path, file);
//            movieDto.setPoster(savedFileName);
            savedMovie.setPoster(savedFileName);
        }

//        Movie updatedMovie = new Movie(movieId, movieDto.getTitle(), movieDto.getDirector(), movieDto.getStudio(), movieDto.getMovieCast(),
//                movieDto.getReleaseYear(), movieDto.getPoster());

        savedMovie.setTitle(movieDto.getTitle());
        savedMovie.setDirector(movieDto.getDirector());
        savedMovie.setStudio(movieDto.getStudio());
        savedMovie.setMovieCast(movieDto.getMovieCast());
        savedMovie.setReleaseYear(movieDto.getReleaseYear());

        Movie savedUpdatedMovie = movieRepo.save(savedMovie);

        String posterUrl = baseUrl + "/file/" + savedFileName;

//        MovieDto newDto = modelMapper.map(updatedMovie, MovieDto.class);
        MovieDto newDto = new MovieDto(savedUpdatedMovie.getMovieId(),
                savedUpdatedMovie.getTitle(), savedUpdatedMovie.getDirector(), savedUpdatedMovie.getStudio(), savedUpdatedMovie.getMovieCast(), savedUpdatedMovie.getReleaseYear(), savedUpdatedMovie.getPoster(), posterUrl);


        return newDto;

    }

    @Override
    public String deleteMovie(Integer movieId) throws IOException {
        Movie savedMovie = movieRepo.findById(movieId).orElseThrow(() -> new MovieNotFoundException("Movie Not Found with id "+ movieId));
        Files.deleteIfExists(Paths.get(path + File.separator + savedMovie.getPoster()));
        movieRepo.deleteById(movieId);
        return "Movie successfully deleted with id " + movieId;
    }

    @Override
    public MoviePageResponse getAllMoviesWithPagination(Integer pageNumber, Integer pageSize) {

        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<Movie> moviePages = movieRepo.findAll(pageable);
        List<Movie> movies = moviePages.getContent();
        
        List<MovieDto> movieDtos = new ArrayList<>();

        for(Movie savedMovie: movies){
            String posterUrl = baseUrl + "/file/" + savedMovie.getPoster();
            movieDtos.add(new MovieDto(savedMovie.getMovieId(),
                    savedMovie.getTitle(), savedMovie.getDirector(), savedMovie.getStudio(), savedMovie.getMovieCast(), savedMovie.getReleaseYear(), savedMovie.getPoster(), posterUrl));
        }
        return new MoviePageResponse(movieDtos, pageNumber, pageSize, moviePages.getTotalElements(), moviePages.getTotalPages(), moviePages.isLast());
    }

    @Override
    public MoviePageResponse getAllMoviesWithPaginationAndSorting(Integer pageNumber, Integer pageSize, String sortBy, String dir) {

        Sort sort = dir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
        Page<Movie> moviePages = movieRepo.findAll(pageable);
        List<Movie> movies = moviePages.getContent();

        List<MovieDto> movieDtos = new ArrayList<>();

        for(Movie savedMovie: movies){
            String posterUrl = baseUrl + "/file/" + savedMovie.getPoster();
            movieDtos.add(new MovieDto(savedMovie.getMovieId(),
                    savedMovie.getTitle(), savedMovie.getDirector(), savedMovie.getStudio(), savedMovie.getMovieCast(), savedMovie.getReleaseYear(), savedMovie.getPoster(), posterUrl));
        }
        return new MoviePageResponse(movieDtos, pageNumber, pageSize,  moviePages.getTotalElements(),moviePages.getTotalPages(), moviePages.isLast());

    }
}
