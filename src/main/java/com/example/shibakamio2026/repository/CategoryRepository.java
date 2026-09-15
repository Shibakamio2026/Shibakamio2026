package com.example.shibakamio2026.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.shibakamio2026.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {

	List<Category> findByUser_UserIdOrderByCategoryIdAsc(Long userId);

	Optional<Category> findByCategoryIdAndUser_UserId(Long categoryId, Long userId);

	boolean existsByUser_UserIdAndCategoryNameIgnoreCase(Long userId, String categoryName);

	boolean existsByUser_UserIdAndCategoryNameIgnoreCaseAndCategoryIdNot(
			Long userId, String categoryName, Long categoryId);

	/**
	 * カテゴリーが確定済み収支（transactions）で使用されているか。
	 * transactions エンティティは収支管理モジュールの担当のため、ネイティブSQLで直接参照する。
	 */
	@Query(value = "SELECT COUNT(*) FROM transactions WHERE category_id = :categoryId", nativeQuery = true)
	long countUsageInTransactions(@Param("categoryId") Long categoryId);

	/** 自動収支（auto_transactions）で使用されているか。自動収支モジュールの担当テーブル。 */
	@Query(value = "SELECT COUNT(*) FROM auto_transactions WHERE category_id = :categoryId", nativeQuery = true)
	long countUsageInAutoTransactions(@Param("categoryId") Long categoryId);

	/** 予定収支（scheduled_transactions）で使用されているか。自動収支モジュールの担当テーブル。 */
	@Query(value = "SELECT COUNT(*) FROM scheduled_transactions WHERE category_id = :categoryId", nativeQuery = true)
	long countUsageInScheduledTransactions(@Param("categoryId") Long categoryId);
}
