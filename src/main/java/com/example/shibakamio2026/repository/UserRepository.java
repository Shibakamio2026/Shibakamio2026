package com.example.shibakamio2026.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.shibakamio2026.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByEmail(String email);
}