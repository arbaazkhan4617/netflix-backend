package com.netflix.backend.service;

import java.util.List;

import com.netflix.backend.entity.Profile;

public interface ProfileService {

    Profile createProfile(Profile profile);

    List<Profile> getProfilesByUserId(Long appUserId);

    long countProfilesByUser(Long appUserId);
}
