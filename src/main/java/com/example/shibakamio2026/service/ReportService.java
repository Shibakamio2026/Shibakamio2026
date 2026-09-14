package com.example.shibakamio2026.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.shibakamio2026.dto.AssetBalanceSnapshot;
import com.example.shibakamio2026.dto.CategorySummary;
import com.example.shibakamio2026.dto.MonthlyChartResponse;
import com.example.shibakamio2026.dto.MonthlyDetailView;
import com.example.shibakamio2026.dto.MonthlySummary;
import com.example.shibakamio2026.repository.ReportQueryRepository;

import lombok.RequiredArgsConstructor;

/**
 * 分析・レポートのサービスクラス。G06 分析・レポート / G14 月々のグラフ表示 / JSON出力API に対応する。
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

	private final ReportQueryRepository reportQueryRepository;

	/**
	 * from〜to（両端含む）の各月について、確定済み収支・未確定の予定収支を集計する。
	 * データが存在しない月も0円として結果に含める（要件14.3：データなしはエラーではなく表示状態）。
	 */
	public List<MonthlySummary> getMonthlySummaries(Long userId, YearMonth from, YearMonth to) {
		LocalDate fromDate = from.atDay(1);
		LocalDate toDate = to.atEndOfMonth();

		Map<String, MonthlySummary> byMonth = new LinkedHashMap<>();
		YearMonth cursor = from;
		while (!cursor.isAfter(to)) {
			MonthlySummary s = new MonthlySummary();
			s.setYearMonth(cursor.toString());
			byMonth.put(cursor.toString(), s);
			cursor = cursor.plusMonths(1);
		}

		for (Object[] row : reportQueryRepository.monthlyConfirmedSums(userId, fromDate, toDate)) {
			MonthlySummary s = byMonth.get((String) row[0]);
			if (s == null) {
				continue;
			}
			applyAmount(s, (String) row[1], (BigDecimal) row[2], true);
		}

		for (Object[] row : reportQueryRepository.monthlyScheduledSums(userId, fromDate, toDate)) {
			MonthlySummary s = byMonth.get((String) row[0]);
			if (s == null) {
				continue;
			}
			applyAmount(s, (String) row[1], (BigDecimal) row[2], false);
		}

		List<MonthlySummary> result = new ArrayList<>(byMonth.values());
		applyMonthlyBarPercents(result);
		return result;
	}

	private void applyAmount(MonthlySummary s, String transactionType, BigDecimal amount, boolean confirmed) {
		if (confirmed) {
			if ("INCOME".equals(transactionType)) {
				s.setConfirmedIncome(amount);
			} else if ("EXPENSE".equals(transactionType)) {
				s.setConfirmedExpense(amount);
			}
		} else {
			if ("INCOME".equals(transactionType)) {
				s.setScheduledIncome(amount);
			} else if ("EXPENSE".equals(transactionType)) {
				s.setScheduledExpense(amount);
			}
		}
	}

	private void applyMonthlyBarPercents(List<MonthlySummary> list) {
		BigDecimal max = list.stream()
				.flatMap(s -> Stream.of(s.getConfirmedIncome(), s.getConfirmedExpense()))
				.max(Comparator.naturalOrder())
				.orElse(BigDecimal.ZERO);
		for (MonthlySummary s : list) {
			s.setIncomeBarPercent(percentOf(s.getConfirmedIncome(), max));
			s.setExpenseBarPercent(percentOf(s.getConfirmedExpense(), max));
		}
	}

	/** カテゴリー別の確定済み収支を集計する（要件7.3）。 */
	public List<CategorySummary> getCategorySummaries(Long userId, YearMonth from, YearMonth to) {
		List<CategorySummary> list = new ArrayList<>();
		for (Object[] row : reportQueryRepository.categorySums(userId, from.atDay(1), to.atEndOfMonth())) {
			CategorySummary c = new CategorySummary();
			c.setCategoryName((String) row[0]);
			c.setTransactionType((String) row[1]);
			c.setAmount((BigDecimal) row[2]);
			list.add(c);
		}
		BigDecimal max = list.stream()
				.map(CategorySummary::getAmount)
				.max(Comparator.naturalOrder())
				.orElse(BigDecimal.ZERO);
		for (CategorySummary c : list) {
			c.setBarPercent(percentOf(c.getAmount(), max));
		}
		return list;
	}

	/** 有効な資産の現在残高一覧（要件7.2「資産残高の推移」の簡易版）。 */
	public List<AssetBalanceSnapshot> getAssetBalances(Long userId) {
		List<AssetBalanceSnapshot> list = new ArrayList<>();
		for (Object[] row : reportQueryRepository.assetBalanceSnapshot(userId)) {
			list.add(new AssetBalanceSnapshot((String) row[0], (BigDecimal) row[1]));
		}
		return list;
	}

	/** G14 月々のグラフ表示 用に、指定した1か月分の詳細をまとめて取得する。 */
	public MonthlyDetailView getMonthlyDetail(Long userId, YearMonth month) {
		MonthlySummary summary = getMonthlySummaries(userId, month, month).get(0);
		List<CategorySummary> categories = getCategorySummaries(userId, month, month);
		List<AssetBalanceSnapshot> assets = getAssetBalances(userId);
		return new MonthlyDetailView(summary, categories, assets);
	}

	/** JSON出力（要件8章）用のレスポンスを組み立てる。 */
	public MonthlyChartResponse buildJsonExport(Long userId, YearMonth from, YearMonth to) {
		List<MonthlySummary> months = getMonthlySummaries(userId, from, to);
		List<CategorySummary> categories = getCategorySummaries(userId, from, to);
		List<AssetBalanceSnapshot> assets = getAssetBalances(userId);
		return new MonthlyChartResponse(from.toString(), to.toString(), months, categories, assets);
	}

	private double percentOf(BigDecimal value, BigDecimal max) {
		if (max == null || max.compareTo(BigDecimal.ZERO) <= 0) {
			return 0d;
		}
		return value.multiply(BigDecimal.valueOf(100))
				.divide(max, 2, RoundingMode.HALF_UP)
				.doubleValue();
	}
}
