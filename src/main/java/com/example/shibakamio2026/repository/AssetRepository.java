package com.example.shibakamio2026.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.shibakamio2026.entity.Asset;

public interface AssetRepository extends JpaRepository<Asset, Long> {

	List<Asset> findByUser_UserIdOrderByAssetIdAsc(Long userId);

	Optional<Asset> findByAssetIdAndUser_UserId(Long assetId, Long userId);

	boolean existsByUser_UserIdAndAssetNameIgnoreCase(Long userId, String assetName);

	/**
	 * 指定資産の確定済み収入合計（transactions テーブル）。
	 * transactions エンティティは収支管理モジュールの担当のため、
	 * モジュール間の結合を避けるためにネイティブSQLで直接参照する。
	 */
	@Query(value = "SELECT COALESCE(SUM(amount), 0) FROM transactions " +
			"WHERE asset_id = :assetId AND transaction_type = 'INCOME'", nativeQuery = true)
	BigDecimal sumConfirmedIncome(@Param("assetId") Long assetId);

	/** 指定資産の確定済み支出合計（transactions テーブル）。 */
	@Query(value = "SELECT COALESCE(SUM(amount), 0) FROM transactions " +
			"WHERE asset_id = :assetId AND transaction_type = 'EXPENSE'", nativeQuery = true)
	BigDecimal sumConfirmedExpense(@Param("assetId") Long assetId);

	/**
	 * 指定資産にひもづく未確定（予定・確認待ち）の予定収入合計（scheduled_transactions テーブル）。
	 * scheduled_transactions エンティティは自動収支モジュールの担当のため、同様にネイティブSQLで参照する。
	 */
	@Query(value = "SELECT COALESCE(SUM(amount), 0) FROM scheduled_transactions " +
			"WHERE asset_id = :assetId AND transaction_type = 'INCOME' " +
			"AND status IN ('PLANNED', 'PENDING_CONFIRMATION')", nativeQuery = true)
	BigDecimal sumScheduledIncome(@Param("assetId") Long assetId);

	/** 指定資産にひもづく未確定（予定・確認待ち）の予定支出合計（scheduled_transactions テーブル）。 */
	@Query(value = "SELECT COALESCE(SUM(amount), 0) FROM scheduled_transactions " +
			"WHERE asset_id = :assetId AND transaction_type = 'EXPENSE' " +
			"AND status IN ('PLANNED', 'PENDING_CONFIRMATION')", nativeQuery = true)
	BigDecimal sumScheduledExpense(@Param("assetId") Long assetId);
}
