package com.example.demo.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Transaction;
import com.example.demo.entity.User;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
	List<Transaction> findByAsset_UserOrderByTransactionDateDesc(User user);

	List<Transaction> findByAsset_UserAndTransactionDateBetweenOrderByTransactionDateAsc(
			User user, LocalDate start, LocalDate end);
}