package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Transaction;
import com.example.demo.entity.User;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
	List<Transaction> findByAsset_UserOrderByTransactionDateDesc(User user);
}