package com.example.shibakamio2026.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.shibakamio2026.entity.AutoTransaction;
import com.example.shibakamio2026.entity.ScheduledTransaction;
import com.example.shibakamio2026.entity.User;
import com.example.shibakamio2026.enums.ScheduledStatus;

public interface ScheduledTransactionRepository extends JpaRepository<ScheduledTransaction, Long> {
	List<ScheduledTransaction> findByAsset_UserAndStatus(User user, ScheduledStatus status);

	List<ScheduledTransaction> findByAutoTransaction(AutoTransaction autoTransaction);
}