package com.netflix.backend.service;

import java.util.List;
import java.util.Optional;

import com.netflix.backend.entity.Series;

public interface SeriesService {

    Series save(Series series);

    Optional<Series> getById(Long id);

    List<Series> getAll();

    List<Series> searchByTitle(String title);

    List<Series> getByReleaseYear(Integer year);
}
