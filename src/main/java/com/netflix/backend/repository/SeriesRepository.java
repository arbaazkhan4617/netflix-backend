package com.netflix.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.netflix.backend.entity.Series;

@Repository
public interface SeriesRepository extends JpaRepository<Series, Long> {

	List<Series> findByReleaseYear(Integer releaseYear);

	List<Series> findByTitleContainingIgnoreCase(String title);
}
