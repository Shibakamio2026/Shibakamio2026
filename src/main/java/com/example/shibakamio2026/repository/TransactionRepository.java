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

	// カレンダー表示用。category を fetch join して N+1 を防ぎ、同じ日の中の並び順も transactionId で固定する。
	@Query("select t from Transaction t "
			+ "join fetch t.asset a "
			+ "left join fetch t.category "
			+ "where a.user = :user "
			+ "and t.transactionDate between :start and :end "
			+ "order by t.transactionDate asc, t.transactionId asc")
	List<Transaction> findForCalendar(@Param("user") User user,
			@Param("start") LocalDate start,
			@Param("end") LocalDate end);
}