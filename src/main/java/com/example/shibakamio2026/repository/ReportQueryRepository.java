package com.example.shibakamio2026.repository;

import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.springframework.stereotype.Repository;

/**
 * 分析・レポート（G06, G14）で使用する集計クエリ。
 *
 * transactions / scheduled_transactions / assets / categories は、いずれも他モジュール
 * （収支管理・自動収支・資産管理・設定）が担当するテーブルであり、このモジュールでは
 * それらのエンティティクラスに依存せず、EntityManager からネイティブSQLで直接集計する。
 * これにより、他モジュールの実装状況に関わらずこのモジュール単体でコンパイル・動作確認ができる。
 */
@Repository
public class ReportQueryRepository {

	@PersistenceContext
	private EntityManager em;

	/**
	 * 確定済み収支（transactions）を月・取引種別ごとに集計する。
	 * 戻り値は Object[]{ yearMonth(String), transactionType(String), amount(BigDecimal) } のリスト。
	 */
	@SuppressWarnings("unchecked")
	public List<Object[]> monthlyConfirmedSums(Long userId, LocalDate from, LocalDate to) {
		String sql = "SELECT to_char(t.transaction_date, 'YYYY-MM') AS ym, t.transaction_type, SUM(t.amount) " +
				"FROM transactions t JOIN assets a ON t.asset_id = a.asset_id " +
				"WHERE a.user_id = :userId AND t.transaction_date BETWEEN :from AND :to " +
				"GROUP BY ym, t.transaction_type ORDER BY ym";
		return em.createNativeQuery(sql)
				.setParameter("userId", userId)
				.setParameter("from", from)
				.setParameter("to", to)
				.getResultList();
	}

	/**
	 * 未確定（予定・確認待ち）の予定収支（scheduled_transactions）を月・取引種別ごとに集計する。
	 * 戻り値は monthlyConfirmedSums と同じ形式。
	 */
	@SuppressWarnings("unchecked")
	public List<Object[]> monthlyScheduledSums(Long userId, LocalDate from, LocalDate to) {
		String sql = "SELECT to_char(st.scheduled_date, 'YYYY-MM') AS ym, st.transaction_type, SUM(st.amount) " +
				"FROM scheduled_transactions st JOIN assets a ON st.asset_id = a.asset_id " +
				"WHERE a.user_id = :userId " +
				"AND st.status IN ('PLANNED', 'PENDING_CONFIRMATION') " +
				"AND st.scheduled_date BETWEEN :from AND :to " +
				"GROUP BY ym, st.transaction_type ORDER BY ym";
		return em.createNativeQuery(sql)
				.setParameter("userId", userId)
				.setParameter("from", from)
				.setParameter("to", to)
				.getResultList();
	}

	/**
	 * カテゴリー別の確定済み収支を集計する（要件7.3：集計対象は確定済みの収支のみ）。
	 * 戻り値は Object[]{ categoryName(String), transactionType(String), amount(BigDecimal) } のリスト。
	 */
	@SuppressWarnings("unchecked")
	public List<Object[]> categorySums(Long userId, LocalDate from, LocalDate to) {
		String sql = "SELECT c.category_name, t.transaction_type, SUM(t.amount) " +
				"FROM transactions t " +
				"JOIN assets a ON t.asset_id = a.asset_id " +
				"JOIN categories c ON t.category_id = c.category_id " +
				"WHERE a.user_id = :userId AND t.transaction_date BETWEEN :from AND :to " +
				"GROUP BY c.category_name, t.transaction_type " +
				"ORDER BY c.category_name";
		return em.createNativeQuery(sql)
				.setParameter("userId", userId)
				.setParameter("from", from)
				.setParameter("to", to)
				.getResultList();
	}

	/**
	 * 資産ごとの現在残高スナップショット（要件9.2の計算式を再現）。
	 * 戻り値は Object[]{ assetName(String), currentBalance(BigDecimal) } のリスト。
	 */
	@SuppressWarnings("unchecked")
	public List<Object[]> assetBalanceSnapshot(Long userId) {
		String sql = "SELECT a.asset_name, " +
				"  a.initial_balance " +
				"  + COALESCE((SELECT SUM(t.amount) FROM transactions t WHERE t.asset_id = a.asset_id AND t.transaction_type = 'INCOME'), 0) "
				+
				"  - COALESCE((SELECT SUM(t.amount) FROM transactions t WHERE t.asset_id = a.asset_id AND t.transaction_type = 'EXPENSE'), 0) "
				+
				"  + COALESCE((SELECT SUM(tr.amount) FROM transfers tr WHERE tr.to_asset_id = a.asset_id), 0) " +
				"  - COALESCE((SELECT SUM(tr.amount) FROM transfers tr WHERE tr.from_asset_id = a.asset_id), 0) " +
				"  AS current_balance " +
				"FROM assets a " +
				"WHERE a.user_id = :userId AND a.is_active = true " +
				"ORDER BY a.asset_id";
		return em.createNativeQuery(sql)
				.setParameter("userId", userId)
				.getResultList();
	}
}
