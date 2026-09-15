package com.example.shibakamio2026.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.shibakamio2026.dto.DashboardCategorySlice;
import com.example.shibakamio2026.dto.DashboardMonthPoint;
import com.example.shibakamio2026.entity.Asset;
import com.example.shibakamio2026.entity.ScheduledTransaction;
import com.example.shibakamio2026.entity.Transaction;
import com.example.shibakamio2026.entity.User;
import com.example.shibakamio2026.enums.ScheduledStatus;
import com.example.shibakamio2026.enums.TransactionType;
import com.example.shibakamio2026.repository.AssetRepository;
import com.example.shibakamio2026.repository.ScheduledTransactionRepository;
import com.example.shibakamio2026.repository.TransactionRepository;
import com.example.shibakamio2026.repository.UserRepository;

@Controller
public class HomeController {

	/** グラフの表示期間（当月を含む直近Nか月） */
	private static final int CHART_MONTHS = 6;

	/** 円グラフに個別表示するカテゴリー数。これを超えた分は「その他」にまとめる */
	private static final int PIE_MAX_SLICES = 5;

	/** 円グラフの配色 */
	private static final List<String> PIE_COLORS = List.of(
			"#4caf7d", "#7fd0a5", "#ffa94d", "#e05c5c", "#6fa8dc", "#b0bfb7");

	private final UserRepository userRepository;
	private final AssetRepository assetRepository;
	private final TransactionRepository transactionRepository;
	private final ScheduledTransactionRepository scheduledTransactionRepository;

	public HomeController(UserRepository userRepository,
			AssetRepository assetRepository,
			TransactionRepository transactionRepository,
			ScheduledTransactionRepository scheduledTransactionRepository) {
		this.userRepository = userRepository;
		this.assetRepository = assetRepository;
		this.transactionRepository = transactionRepository;
		this.scheduledTransactionRepository = scheduledTransactionRepository;
	}

	@GetMapping("/home")
	public String home(Authentication authentication, Model model) {
		User user = userRepository.findByEmailIgnoreCase(authentication.getName())
				.orElseThrow(() -> new IllegalStateException("ユーザーが見つかりません"));

		List<Asset> assets = assetRepository.findByUserAndIsActiveTrueOrderByAssetIdAsc(user);
		List<Transaction> allTransactions = transactionRepository.findByAsset_UserOrderByTransactionDateDesc(user);

		Map<Long, List<Transaction>> byAsset = allTransactions.stream()
				.collect(Collectors.groupingBy(t -> t.getAsset().getAssetId()));

		Map<Asset, BigDecimal> assetBalances = new LinkedHashMap<>();
		BigDecimal totalAssetBalance = BigDecimal.ZERO;

		for (Asset asset : assets) {
			BigDecimal balance = asset.getInitialBalance();
			for (Transaction t : byAsset.getOrDefault(asset.getAssetId(), List.of())) {
				balance = t.getTransactionType() == TransactionType.INCOME
						? balance.add(t.getAmount())
						: balance.subtract(t.getAmount());
			}
			assetBalances.put(asset, balance);
			totalAssetBalance = totalAssetBalance.add(balance);
		}

		YearMonth currentMonth = YearMonth.now();
		BigDecimal monthlyIncome = BigDecimal.ZERO;
		BigDecimal monthlyExpense = BigDecimal.ZERO;
		for (Transaction t : allTransactions) {
			if (YearMonth.from(t.getTransactionDate()).equals(currentMonth)) {
				if (t.getTransactionType() == TransactionType.INCOME) {
					monthlyIncome = monthlyIncome.add(t.getAmount());
				} else {
					monthlyExpense = monthlyExpense.add(t.getAmount());
				}
			}
		}

		List<ScheduledTransaction> plannedThisMonth = scheduledTransactionRepository
				.findByAsset_UserAndStatus(user, ScheduledStatus.PLANNED).stream()
				.filter(s -> YearMonth.from(s.getScheduledDate()).equals(currentMonth))
				.collect(Collectors.toList());

		BigDecimal scheduledNet = plannedThisMonth.stream()
				.map(s -> s.getTransactionType() == TransactionType.INCOME ? s.getAmount() : s.getAmount().negate())
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		BigDecimal projectedBalance = totalAssetBalance.add(scheduledNet);

		model.addAttribute("userName", user.getUserName());
		model.addAttribute("totalAssetBalance", totalAssetBalance);
		model.addAttribute("monthlyIncome", monthlyIncome);
		model.addAttribute("monthlyExpense", monthlyExpense);
		model.addAttribute("projectedBalance", projectedBalance);
		model.addAttribute("recentTransactions", allTransactions.stream().limit(5).collect(Collectors.toList()));
		model.addAttribute("assetBalances", assetBalances);
		model.addAttribute("scheduledCount", plannedThisMonth.size());
		model.addAttribute("scheduledNet", scheduledNet);

		// ------------------------------------------------------------
		// グラフ用データ（資産推移 / 月刊収支 / 今月の支出内訳）
		// ------------------------------------------------------------
		List<DashboardMonthPoint> monthPoints = buildMonthPoints(allTransactions, totalAssetBalance, currentMonth);
		boolean hasMonthlyData = monthPoints.stream()
				.anyMatch(p -> p.getIncome().signum() != 0 || p.getExpense().signum() != 0);

		List<DashboardCategorySlice> expenseSlices = buildExpenseSlices(allTransactions, currentMonth);

		model.addAttribute("monthPoints", monthPoints);
		model.addAttribute("hasMonthlyData", hasMonthlyData);
		model.addAttribute("hasAssetData", !assets.isEmpty());
		model.addAttribute("expenseSlices", expenseSlices);
		model.addAttribute("expensePieGradient", buildPieGradient(expenseSlices));
		model.addAttribute("chartMonths", CHART_MONTHS);

		return "home";
	}

	/** 直近CHART_MONTHSか月分の、月別収支と月末時点の総資産額を組み立てる。 */
	private List<DashboardMonthPoint> buildMonthPoints(List<Transaction> allTransactions,
			BigDecimal totalAssetBalance,
			YearMonth currentMonth) {

		YearMonth firstMonth = currentMonth.minusMonths(CHART_MONTHS - 1L);

		Map<YearMonth, BigDecimal[]> byMonth = new LinkedHashMap<>();
		for (YearMonth m = firstMonth; !m.isAfter(currentMonth); m = m.plusMonths(1)) {
			byMonth.put(m, new BigDecimal[] { BigDecimal.ZERO, BigDecimal.ZERO });
		}

		for (Transaction t : allTransactions) {
			BigDecimal[] slot = byMonth.get(YearMonth.from(t.getTransactionDate()));
			if (slot == null) {
				continue;
			}
			if (t.getTransactionType() == TransactionType.INCOME) {
				slot[0] = slot[0].add(t.getAmount());
			} else {
				slot[1] = slot[1].add(t.getAmount());
			}
		}

		// 月末時点の総資産額は、現在の残高からその月末より後の取引を差し引いて逆算する
		Map<YearMonth, BigDecimal> endBalances = new LinkedHashMap<>();
		for (YearMonth m : byMonth.keySet()) {
			final LocalDate endOfMonth = m.atEndOfMonth();
			BigDecimal afterMonth = allTransactions.stream()
					.filter(t -> t.getTransactionDate().isAfter(endOfMonth))
					.map(t -> t.getTransactionType() == TransactionType.INCOME
							? t.getAmount()
							: t.getAmount().negate())
					.reduce(BigDecimal.ZERO, BigDecimal::add);
			endBalances.put(m, totalAssetBalance.subtract(afterMonth));
		}

		BigDecimal maxAmount = byMonth.values().stream()
				.flatMap(a -> Stream.of(a[0], a[1]))
				.max(Comparator.naturalOrder())
				.orElse(BigDecimal.ZERO);

		// 残高はマイナスにもなり得るので、絶対値の最大で正規化する
		BigDecimal maxBalance = endBalances.values().stream()
				.map(BigDecimal::abs)
				.max(Comparator.naturalOrder())
				.orElse(BigDecimal.ZERO);

		List<DashboardMonthPoint> points = new ArrayList<>();
		for (Map.Entry<YearMonth, BigDecimal[]> e : byMonth.entrySet()) {
			YearMonth m = e.getKey();
			BigDecimal income = e.getValue()[0];
			BigDecimal expense = e.getValue()[1];
			BigDecimal balance = endBalances.get(m);

			points.add(new DashboardMonthPoint(
					m.getMonthValue() + "月",
					income,
					expense,
					balance,
					percentOf(income, maxAmount),
					percentOf(expense, maxAmount),
					percentOf(balance.max(BigDecimal.ZERO), maxBalance)));
		}
		return points;
	}

	/** 今月の確定支出をカテゴリー別に集計し、上位PIE_MAX_SLICES件＋「その他」にまとめる。 */
	private List<DashboardCategorySlice> buildExpenseSlices(List<Transaction> allTransactions,
			YearMonth currentMonth) {

		Map<String, BigDecimal> byCategory = new LinkedHashMap<>();
		for (Transaction t : allTransactions) {
			if (t.getTransactionType() != TransactionType.EXPENSE
					|| !YearMonth.from(t.getTransactionDate()).equals(currentMonth)) {
				continue;
			}
			String name = (t.getCategory() != null) ? t.getCategory().getCategoryName() : "未分類";
			byCategory.merge(name, t.getAmount(), BigDecimal::add);
		}

		BigDecimal total = byCategory.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
		if (total.signum() <= 0) {
			return List.of();
		}

		List<Map.Entry<String, BigDecimal>> sorted = byCategory.entrySet().stream()
				.sorted(Map.Entry.<String, BigDecimal> comparingByValue().reversed())
				.collect(Collectors.toList());

		List<DashboardCategorySlice> slices = new ArrayList<>();
		BigDecimal others = BigDecimal.ZERO;

		for (int i = 0; i < sorted.size(); i++) {
			if (i < PIE_MAX_SLICES) {
				BigDecimal amount = sorted.get(i).getValue();
				slices.add(new DashboardCategorySlice(
						sorted.get(i).getKey(),
						amount,
						percentOf(amount, total),
						PIE_COLORS.get(i % PIE_COLORS.size())));
			} else {
				others = others.add(sorted.get(i).getValue());
			}
		}

		if (others.signum() > 0) {
			slices.add(new DashboardCategorySlice(
					"その他",
					others,
					percentOf(others, total),
					PIE_COLORS.get(PIE_COLORS.size() - 1)));
		}
		return slices;
	}

	/** CSSの conic-gradient に渡す色指定の文字列を組み立てる。 */
	private String buildPieGradient(List<DashboardCategorySlice> slices) {
		if (slices.isEmpty()) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		double cursor = 0d;
		for (int i = 0; i < slices.size(); i++) {
			DashboardCategorySlice s = slices.get(i);
			// 端数で隙間ができないよう、最後の1切れは必ず100%で閉じる
			double next = (i == slices.size() - 1) ? 100d : cursor + s.getPercent();
			if (i > 0) {
				sb.append(", ");
			}
			sb.append(s.getColor())
					.append(' ').append(round2(cursor)).append('%')
					.append(' ').append(round2(next)).append('%');
			cursor = next;
		}
		return sb.toString();
	}

	private double percentOf(BigDecimal value, BigDecimal max) {
		if (value == null || max == null || max.signum() <= 0) {
			return 0d;
		}
		return value.multiply(BigDecimal.valueOf(100))
				.divide(max, 2, RoundingMode.HALF_UP)
				.doubleValue();
	}

	private double round2(double value) {
		return Math.round(value * 100d) / 100d;
	}
}