package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.AutoTransaction;
import com.example.demo.entity.User;

public interface AutoTransactionRepository extends JpaRepository<AutoTransaction, Long> {
	List<AutoTransaction> findByAsset_User(User user);
}