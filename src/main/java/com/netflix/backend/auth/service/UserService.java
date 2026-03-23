package com.netflix.backend.auth.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.netflix.backend.entity.User;
import com.netflix.backend.user.repository.UserRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UserService {

	private final UserRepository userRepository;

	public User findByEmail(String email) {
		Optional<User> userOpt = userRepository.findByEmail(email);
		if (userOpt.isPresent()) {
			return userOpt.get();
		}
		return null;
	}

}
