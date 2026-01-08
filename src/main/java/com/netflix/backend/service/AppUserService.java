package com.netflix.backend.service;

import java.util.Optional;

import com.netflix.backend.entity.AppUser;

public interface AppUserService {

    AppUser createUser(AppUser user);

    Optional<AppUser> getByEmail(String email);

    Optional<AppUser> getById(Long id);

    boolean existsByEmail(String email);
}
