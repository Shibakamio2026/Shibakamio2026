package com.example.shibakamio2026.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.shibakamio2026.entity.Asset;
import com.example.shibakamio2026.entity.User;

public interface AssetRepository extends JpaRepository<Asset, Long> {
	List<Asset> findByUserAndActiveTrue(User user);
}