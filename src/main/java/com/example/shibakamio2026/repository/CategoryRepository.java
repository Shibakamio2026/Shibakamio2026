package com.example.shibakamio2026.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.shibakamio2026.entity.Category;
import com.example.shibakamio2026.entity.User;

public interface CategoryRepository extends JpaRepository<Category, Long> {
	List<Category> findByUserAndActiveTrue(User user);

	Optional<Category> findByUserAndCategoryName(User user, String categoryName);
}