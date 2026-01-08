package com.netflix.backend.service.impl;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.netflix.backend.entity.AppUser;
import com.netflix.backend.repository.AppUserRepository;
import com.netflix.backend.service.AppUserService;

@Service
public class AppUserServiceImpl implements AppUserService {

    private final AppUserRepository appUserRepository;

    public AppUserServiceImpl(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @Override
    public AppUser createUser(AppUser user) {
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return appUserRepository.save(user);
    }

    @Override
    public Optional<AppUser> getByEmail(String email) {
        return appUserRepository.findByEmail(email);
    }

    @Override
    public Optional<AppUser> getById(Long id) {
        return appUserRepository.findById(id);
    }

    @Override
    public boolean existsByEmail(String email) {
        return appUserRepository.existsByEmail(email);
    }
}
