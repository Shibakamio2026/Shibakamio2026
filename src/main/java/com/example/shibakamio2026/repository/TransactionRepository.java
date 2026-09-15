package com.example.shibakamio2026.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.shibakamio2026.entity.Transaction;
import com.example.shibakamio2026.entity.User;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

	@Query("select t from Transaction t "
			+ "join fetch t.asset a "
			+ "left join fetch t.category "
			+ "where a.user = :user "
			+ "order by t.transactionDate desc")
	List<Transaction> findByAsset_UserOrderByTransactionDateDesc(@Param("user") User user);

	List<Transaction> findByAsset_UserAndTransactionDateBetweenOrderByTransactionDateAsc(
			User user, LocalDate start, LocalDate end);
}