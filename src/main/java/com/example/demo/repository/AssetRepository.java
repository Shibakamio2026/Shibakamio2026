package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Asset;
import com.example.demo.entity.User;

public interface AssetRepository extends JpaRepository<Asset, Long> {
	List<Asset> findByUserAndActiveTrue(User user);
}