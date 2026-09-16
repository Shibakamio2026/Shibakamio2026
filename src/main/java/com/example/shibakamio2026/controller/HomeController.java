package com.example.shibakamio2026.controller;

import java.math.BigDecimal;
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

	/** ダッシュボードのグラフに表示する月数（資産推移・月刊収支とも共通） */
	private static final int GRAPH_MONTHS = 6;

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

		// ------------------------------------------------------------
		// ここから：ダッシュボードのグラフ用データ（新規追加）
		// ------------------------------------------------------------

		BigDecimal initialTotal = assets.stream()
				.map(Asset::getInitialBalance)
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		List<Transaction> ascTransactions = new ArrayList<>(allTransactions);
		ascTransactions.sort(Comparator.comparing(Transaction::getTransactionDate));

		List<YearMonth> months = new ArrayList<>();
		for (int i = GRAPH_MONTHS - 1; i >= 0; i--) {
			months.add(currentMonth.minusMonths(i));
		}

		List<String> monthlyLabels = new ArrayList<>();
		List<BigDecimal> monthlyBalances = new ArrayList<>();
		List<BigDecimal> monthlyIncomeSeries = new ArrayList<>();
		List<BigDecimal> monthlyExpenseSeries = new ArrayList<>();

		BigDecimal runningBalance = initialTotal;
		int txIndex = 0;
		for (YearMonth ym : months) {
			LocalDate monthEnd = ym.atEndOfMonth();
			BigDecimal thisMonthIncome = BigDecimal.ZERO;
			BigDecimal thisMonthExpense = BigDecimal.ZERO;

			while (txIndex < ascTransactions.size()
					&& !ascTransactions.get(txIndex).getTransactionDate().isAfter(monthEnd)) {
				Transaction t = ascTransactions.get(txIndex);
				boolean isThisMonth = YearMonth.from(t.getTransactionDate()).equals(ym);

				if (t.getTransactionType() == TransactionType.INCOME) {
					runningBalance = runningBalance.add(t.getAmount());
					if (isThisMonth) {
						thisMonthIncome = thisMonthIncome.add(t.getAmount());
					}
				} else {
					runningBalance = runningBalance.subtract(t.getAmount());
					if (isThisMonth) {
						thisMonthExpense = thisMonthExpense.add(t.getAmount());
					}
				}
				txIndex++;
			}

			monthlyLabels.add(ym.getMonthValue() + "月");
			monthlyBalances.add(runningBalance);
			monthlyIncomeSeries.add(thisMonthIncome);
			monthlyExpenseSeries.add(thisMonthExpense);
		}

		// 今月の支出を、カテゴリー別に集計（円グラフ用）
		Map<String, BigDecimal> categoryTotals = new LinkedHashMap<>();
		for (Transaction t : allTransactions) {
			if (t.getTransactionType() == TransactionType.EXPENSE
					&& YearMonth.from(t.getTransactionDate()).equals(currentMonth)) {
				categoryTotals.merge(t.getCategory().getCategoryName(), t.getAmount(), BigDecimal::add);
			}
		}

		List<String> categoryNames = new ArrayList<>(categoryTotals.keySet());
		List<BigDecimal> categoryAmounts = new ArrayList<>(categoryTotals.values());

		// ------------------------------------------------------------
		// ここまで：ダッシュボードのグラフ用データ
		// ------------------------------------------------------------

		model.addAttribute("userName", user.getUserName());
		model.addAttribute("totalAssetBalance", totalAssetBalance);
		model.addAttribute("monthlyIncome", monthlyIncome);
		model.addAttribute("monthlyExpense", monthlyExpense);
		model.addAttribute("projectedBalance", projectedBalance);
		model.addAttribute("recentTransactions", allTransactions.stream().limit(5).collect(Collectors.toList()));
		model.addAttribute("assetBalances", assetBalances);
		model.addAttribute("scheduledCount", plannedThisMonth.size());
		model.addAttribute("scheduledNet", scheduledNet);

		// グラフ用（新規）
		model.addAttribute("monthlyLabels", monthlyLabels);
		model.addAttribute("monthlyBalances", monthlyBalances);
		model.addAttribute("monthlyIncomeSeries", monthlyIncomeSeries);
		model.addAttribute("monthlyExpenseSeries", monthlyExpenseSeries);
		model.addAttribute("categoryNames", categoryNames);
		model.addAttribute("categoryAmounts", categoryAmounts);
		model.addAttribute("hasCategoryData", !categoryNames.isEmpty());
		model.addAttribute("hasAnyTransaction", !allTransactions.isEmpty());

		return "home";
	}
}