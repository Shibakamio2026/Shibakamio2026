package com.example.demo.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.dto.SignupForm;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;

@Service
public class AccountService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public AccountService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	public void registerUser(SignupForm form) {
		User user = new User();
		user.setUserName(form.getUserName());
		user.setEmail(form.getEmail());
		user.setPasswordHash(passwordEncoder.encode(form.getPassword()));
		userRepository.save(user);
	}

	public boolean isEmailTaken(String email) {
		return userRepository.findByEmail(email).isPresent();
	}
}