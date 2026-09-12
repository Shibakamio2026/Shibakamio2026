package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Category;
import com.example.demo.entity.User;

public interface CategoryRepository extends JpaRepository<Category, Long> {
	List<Category> findByUserAndActiveTrue(User user);

	Optional<Category> findByUserAndCategoryName(User user, String categoryName);
}