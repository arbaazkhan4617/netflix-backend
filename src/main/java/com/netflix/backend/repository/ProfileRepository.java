package com.netflix.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.netflix.backend.entity.Profile;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {

	List<Profile> findByAppUserId(Long appUserId);

	long countByAppUserId(Long appUserId);
}
