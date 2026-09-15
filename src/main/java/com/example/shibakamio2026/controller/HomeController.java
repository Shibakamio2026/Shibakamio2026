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

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.shibakamio2026.dto.CategorySummary;
import com.example.shibakamio2026.dto.ChartPointView;
import com.example.shibakamio2026.dto.MonthlySummary;
import com.example.shibakamio2026.dto.PieSliceView;
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
import com.example.shibakamio2026.service.ReportService;

@Controller
public class HomeController {

	/** グラフの表示月数（当月を含む直近Nか月） */
	private static final int CHART_MONTHS = 6;

	/** 円グラフの色パレット。カテゴリー数がこれを超えたら先頭から使い回す。 */
	private static final List<String> PIE_COLORS = List.of(
			"#4caf7d", "#ffa94d", "#e05c5c", "#6aa9e0", "#b08ce0", "#e0c14c", "#5ec8b8", "#e08cb0");

	private final UserRepository userRepository;
	private final AssetRepository assetRepository;
	private final TransactionRepository transactionRepository;
	private final ScheduledTransactionRepository scheduledTransactionRepository;
	private final ReportService reportService;

	public HomeController(UserRepository userRepository,
			AssetRepository assetRepository,
			TransactionRepository transactionRepository,
			ScheduledTransactionRepository scheduledTransactionRepository,
			ReportService reportService) {
		this.userRepository = userRepository;
		this.assetRepository = assetRepository;
		this.transactionRepository = transactionRepository;
		this.scheduledTransactionRepository = scheduledTransactionRepository;
		this.reportService = reportService;
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

		// ===== グラフ用データ =====
		YearMonth fromMonth = currentMonth.minusMonths(CHART_MONTHS - 1);

		// ① 資産推移：各月末時点の総資産残高
		List<ChartPointView> assetTrend = buildAssetTrend(assets, allTransactions, fromMonth, currentMonth);

		// ② 月間収支：確定済みの収入・支出（ReportServiceが月ごとの棒の高さ%まで計算済み）
		List<MonthlySummary> monthlySummaries = reportService.getMonthlySummaries(
				user.getUserId(), fromMonth, currentMonth);

		// ③ 今月の支出内訳（円グラフ）
		List<PieSliceView> expensePie = buildExpensePie(
				reportService.getCategorySummaries(user.getUserId(), currentMonth, currentMonth));

		model.addAttribute("userName", user.getUserName());
		model.addAttribute("totalAssetBalance", totalAssetBalance);
		model.addAttribute("monthlyIncome", monthlyIncome);
		model.addAttribute("monthlyExpense", monthlyExpense);
		model.addAttribute("projectedBalance", projectedBalance);
		model.addAttribute("recentTransactions", allTransactions.stream().limit(5).collect(Collectors.toList()));
		model.addAttribute("assetBalances", assetBalances);
		model.addAttribute("scheduledCount", plannedThisMonth.size());
		model.addAttribute("scheduledNet", scheduledNet);

		model.addAttribute("assetTrend", assetTrend);
		model.addAttribute("monthlySummaries", monthlySummaries);
		model.addAttribute("expensePie", expensePie);
		model.addAttribute("expensePieGradient", buildPieGradient(expensePie));

		return "home";
	}

	/** 各月末時点の総資産残高を計算する。取引が0件でも月は並ぶ（0円として表示）。 */
	private List<ChartPointView> buildAssetTrend(List<Asset> assets,
			List<Transaction> allTransactions,
			YearMonth from,
			YearMonth to) {

		BigDecimal initialTotal = assets.stream()
				.map(Asset::getInitialBalance)
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		List<ChartPointView> points = new ArrayList<>();
		YearMonth cursor = from;
		while (!cursor.isAfter(to)) {
			LocalDate monthEnd = cursor.atEndOfMonth();
			BigDecimal balance = initialTotal;
			for (Transaction t : allTransactions) {
				if (!t.getTransactionDate().isAfter(monthEnd)) {
					balance = t.getTransactionType() == TransactionType.INCOME
							? balance.add(t.getAmount())
							: balance.subtract(t.getAmount());
				}
			}
			points.add(new ChartPointView(cursor.getMonthValue() + "月", balance, 0d));
			cursor = cursor.plusMonths(1);
		}

		BigDecimal max = points.stream()
				.map(p -> p.getValue().abs())
				.max(Comparator.naturalOrder())
				.orElse(BigDecimal.ZERO);

		for (ChartPointView p : points) {
			p.setPercent(percentOf(p.getValue().max(BigDecimal.ZERO), max));
		}
		return points;
	}

	/** カテゴリー別集計から、支出だけを取り出して割合を計算する。 */
	private List<PieSliceView> buildExpensePie(List<CategorySummary> categories) {
		List<CategorySummary> expenses = categories.stream()
				.filter(c -> "EXPENSE".equals(c.getTransactionType()))
				.sorted(Comparator.comparing(CategorySummary::getAmount).reversed())
				.collect(Collectors.toList());

		BigDecimal total = expenses.stream()
				.map(CategorySummary::getAmount)
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		List<PieSliceView> slices = new ArrayList<>();
		for (int i = 0; i < expenses.size(); i++) {
			CategorySummary c = expenses.get(i);
			slices.add(new PieSliceView(
					c.getCategoryName(),
					c.getAmount(),
					percentOf(c.getAmount(), total),
					PIE_COLORS.get(i % PIE_COLORS.size())));
		}
		return slices;
	}

	/** CSSの conic-gradient 用の文字列を組み立てる（例："#4caf7d 0% 40%, #ffa94d 40% 100%"）。 */
	private String buildPieGradient(List<PieSliceView> slices) {
		if (slices.isEmpty()) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		double cursor = 0d;
		for (int i = 0; i < slices.size(); i++) {
			PieSliceView s = slices.get(i);
			double next = (i == slices.size() - 1) ? 100d : cursor + s.getPercent();
			if (i > 0) {
				sb.append(", ");
			}
			sb.append(s.getColor())
					.append(' ').append(format(cursor)).append('%')
					.append(' ').append(format(next)).append('%');
			cursor = next;
		}
		return sb.toString();
	}

	private String format(double v) {
		return BigDecimal.valueOf(v).setScale(2, RoundingMode.HALF_UP).toPlainString();
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