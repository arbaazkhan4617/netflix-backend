package com.netflix.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.netflix.backend.entity.Movie;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

	List<Movie> findByReleaseYear(Integer releaseYear);

	List<Movie> findByTitleContainingIgnoreCase(String title);
}
