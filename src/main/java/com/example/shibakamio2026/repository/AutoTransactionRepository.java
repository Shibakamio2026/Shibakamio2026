package com.example.shibakamio2026.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.shibakamio2026.entity.AutoTransaction;
import com.example.shibakamio2026.entity.User;

public interface AutoTransactionRepository extends JpaRepository<AutoTransaction, Long> {
	@Query("select at from AutoTransaction at "
			+ "join fetch at.asset a "
			+ "left join fetch at.category "
			+ "where a.user = :user")
	List<AutoTransaction> findByAsset_User(@Param("user") User user);
}