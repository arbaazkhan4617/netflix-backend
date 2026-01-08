package com.netflix.backend.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.netflix.backend.entity.Series;
import com.netflix.backend.repository.SeriesRepository;
import com.netflix.backend.service.SeriesService;

@Service
public class SeriesServiceImpl implements SeriesService {

    private final SeriesRepository seriesRepository;

    public SeriesServiceImpl(SeriesRepository seriesRepository) {
        this.seriesRepository = seriesRepository;
    }

    @Override
    public Series save(Series series) {
        return seriesRepository.save(series);
    }

    @Override
    public Optional<Series> getById(Long id) {
        return seriesRepository.findById(id);
    }

    @Override
    public List<Series> getAll() {
        return seriesRepository.findAll();
    }

    @Override
    public List<Series> searchByTitle(String title) {
        return seriesRepository.findByTitleContainingIgnoreCase(title);
    }

    @Override
    public List<Series> getByReleaseYear(Integer year) {
        return seriesRepository.findByReleaseYear(year);
    }
}
