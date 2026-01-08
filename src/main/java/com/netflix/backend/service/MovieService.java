package com.netflix.backend.service;

import java.util.List;
import java.util.Optional;

import com.netflix.backend.entity.Movie;

public interface MovieService {

    Movie save(Movie movie);

    Optional<Movie> getById(Long id);

    List<Movie> getAll();

    List<Movie> searchByTitle(String title);

    List<Movie> getByReleaseYear(Integer year);
}
