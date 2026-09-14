package com.example.shibakamio2026.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.shibakamio2026.entity.Transaction;
import com.example.shibakamio2026.entity.User;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
	List<Transaction> findByAsset_UserOrderByTransactionDateDesc(User user);

	List<Transaction> findByAsset_UserAndTransactionDateBetweenOrderByTransactionDateAsc(
			User user, LocalDate start, LocalDate end);
}