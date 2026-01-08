package com.netflix.backend.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.netflix.backend.entity.Profile;
import com.netflix.backend.repository.ProfileRepository;
import com.netflix.backend.service.ProfileService;

@Service
public class ProfileServiceImpl implements ProfileService {

    private final ProfileRepository profileRepository;

    public ProfileServiceImpl(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    @Override
    public Profile createProfile(Profile profile) {
        return profileRepository.save(profile);
    }

    @Override
    public List<Profile> getProfilesByUserId(Long appUserId) {
        return profileRepository.findByAppUserId(appUserId);
    }

    @Override
    public long countProfilesByUser(Long appUserId) {
        return profileRepository.countByAppUserId(appUserId);
    }
}
