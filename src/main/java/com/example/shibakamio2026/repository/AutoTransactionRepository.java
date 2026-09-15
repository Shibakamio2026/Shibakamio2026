package com.example.shibakamio2026.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.shibakamio2026.entity.AutoTransaction;
import com.example.shibakamio2026.entity.User;

public interface AutoTransactionRepository extends JpaRepository<AutoTransaction, Long> {
	List<AutoTransaction> findByAsset_User(User user);
}