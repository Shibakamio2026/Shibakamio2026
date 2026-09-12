package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.AutoTransaction;
import com.example.demo.entity.ScheduledTransaction;
import com.example.demo.entity.User;
import com.example.demo.enums.ScheduledStatus;

public interface ScheduledTransactionRepository extends JpaRepository<ScheduledTransaction, Long> {
	List<ScheduledTransaction> findByAsset_UserAndStatus(User user, ScheduledStatus status);

	List<ScheduledTransaction> findByAutoTransaction(AutoTransaction autoTransaction);
}