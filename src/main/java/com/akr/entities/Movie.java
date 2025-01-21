package com.akr.entities;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.util.Set;

@Entity
@Valid
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Integer movieId;

    @Column(nullable = false)
    @NotBlank(message = "Please provide movie's title")
   private String title;

    @Column(nullable = false)
    @NotBlank(message = "Please provide movie's director")
   private String director;

    @Column(nullable = false)
    @NotBlank(message = "Please provide movie's studio")
   private String studio;

    @ElementCollection
    @CollectionTable(name = "movie_cast")
   private Set<String> movieCast;

    @Column(nullable = false)
   private Integer releaseYear;

    @Column(nullable = false)
    @NotBlank(message = "Please provide movie's poster")
   private String poster;
    public Movie() {
    }

    public Movie(Integer movieId, String title, String director, String studio, Set<String> movieCast, Integer releaseYear, String poster) {
        this.movieId = movieId;
        this.title = title;
        this.director = director;
        this.studio = studio;
        this.movieCast = movieCast;
        this.releaseYear = releaseYear;
        this.poster = poster;
    }
    public Integer getMovieId() {
        return movieId;
    }

    public void setMovieId(Integer movieId) {
        this.movieId = movieId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public String getStudio() {
        return studio;
    }

    public void setStudio(String studio) {
        this.studio = studio;
    }

    public Set<String> getMovieCast() {
        return movieCast;
    }

    public void setMovieCast(Set<String> movieCast) {
        this.movieCast = movieCast;
    }

    public Integer getReleaseYear() {
        return releaseYear;
    }

    public void setReleaseYear(Integer releaseYear) {
        this.releaseYear = releaseYear;
    }

    public String getPoster() {
        return poster;
    }

    public void setPoster( String poster) {
        this.poster = poster;
    }

    @Override
    public String toString() {
        return "Movie{" +
                "movieId=" + movieId +
                ", title='" + title + '\'' +
                ", director='" + director + '\'' +
                ", studio='" + studio + '\'' +
                ", movieCast=" + movieCast +
                ", releaseYear=" + releaseYear +
                ", poster='" + poster + '\'' +
                '}';
    }
}
