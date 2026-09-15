package com.example.shibakamio2026.controller;

import java.math.BigDecimal;
import java.time.YearMonth;
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

		return "home";
	}
}