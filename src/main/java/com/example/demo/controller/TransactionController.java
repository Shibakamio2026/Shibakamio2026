package com.example.demo.controller;

import java.math.BigDecimal;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.demo.dto.TransactionForm;
import com.example.demo.entity.Asset;
import com.example.demo.entity.AutoTransaction;
import com.example.demo.entity.Category;
import com.example.demo.entity.Transaction;
import com.example.demo.entity.User;
import com.example.demo.repository.AssetRepository;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.AutoTransactionService;
import com.example.demo.service.TransactionService;

@Controller
@RequestMapping("/transactions")
public class TransactionController {

	private final UserRepository userRepository;
	private final AssetRepository assetRepository;
	private final CategoryRepository categoryRepository;
	private final TransactionService transactionService;
	private final AutoTransactionService autoTransactionService;

	public TransactionController(UserRepository userRepository,
			AssetRepository assetRepository,
			CategoryRepository categoryRepository,
			TransactionService transactionService,
			AutoTransactionService autoTransactionService) {
		this.userRepository = userRepository;
		this.assetRepository = assetRepository;
		this.categoryRepository = categoryRepository;
		this.transactionService = transactionService;
		this.autoTransactionService = autoTransactionService;
	}

	private User currentUser(Authentication authentication) {
		return userRepository.findByEmail(authentication.getName())
				.orElseThrow(() -> new IllegalStateException("ユーザーが見つかりません"));
	}

	// ===== G04 収支入力 =====

	@GetMapping("/input")
	public String inputForm(Authentication authentication, Model model) {
		User user = currentUser(authentication);
		model.addAttribute("transactionForm", new TransactionForm());
		model.addAttribute("categories", categoryRepository.findByUserAndActiveTrue(user));
		model.addAttribute("assets", assetRepository.findByUserAndActiveTrue(user));
		return "transactions/input";
	}

	@PostMapping("/input")
	public String submit(@ModelAttribute("transactionForm") TransactionForm form,
			Authentication authentication,
			Model model) {

		User user = currentUser(authentication);
		StringBuilder errors = new StringBuilder();

		if (form.getTransactionType() == null) {
			errors.append("取引種別を選択してください。");
		}
		if (form.getAmount() == null || form.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
			errors.append("未入力か０より多くない金額です。");
		}
		if (form.getCategoryId() == null) {
			errors.append("カテゴリーを選択してください。");
		}
		if (form.getAssetId() == null) {
			errors.append("資産を選択してください。");
		}
		if (form.getRegistrationType() == null) {
			errors.append("登録方法を選択してください。");
		}

		boolean isAuto = "AUTO".equals(form.getRegistrationType());

		if (!isAuto && form.getTransactionDate() == null) {
			errors.append("取引日を入力してください。");
		}
		if (isAuto) {
			if (form.getIntervalType() == null) {
				errors.append("発生間隔を選択してください。");
			}
			if (form.getStartDate() == null) {
				errors.append("開始日を入力してください。");
			}
			if (form.getEndDate() != null && form.getStartDate() != null
					&& form.getEndDate().isBefore(form.getStartDate())) {
				errors.append("終了日は開始日以降の日付にしてください。");
			}
		}

		if (errors.length() > 0) {
			model.addAttribute("errorMessage", errors.toString());
			model.addAttribute("categories", categoryRepository.findByUserAndActiveTrue(user));
			model.addAttribute("assets", assetRepository.findByUserAndActiveTrue(user));
			return "transactions/input";
		}

		Category category = categoryRepository.findById(form.getCategoryId())
				.orElseThrow(() -> new IllegalArgumentException("カテゴリーが見つかりません"));
		Asset asset = assetRepository.findById(form.getAssetId())
				.orElseThrow(() -> new IllegalArgumentException("資産が見つかりません"));

		if (isAuto) {
			AutoTransaction auto = new AutoTransaction();
			auto.setAsset(asset);
			auto.setCategory(category);
			auto.setTransactionType(form.getTransactionType());
			auto.setAmount(form.getAmount());
			auto.setIntervalType(form.getIntervalType());
			auto.setStartDate(form.getStartDate());
			auto.setEndDate(form.getEndDate());
			auto.setEnabled(true);
			autoTransactionService.createAutoTransaction(auto);
		} else {
			Transaction transaction = new Transaction();
			transaction.setAsset(asset);
			transaction.setCategory(category);
			transaction.setTransactionType(form.getTransactionType());
			transaction.setTransactionDate(form.getTransactionDate());
			transaction.setAmount(form.getAmount());
			transaction.setMemo(form.getMemo());
			transactionService.registerOneTime(transaction);
		}

		return "redirect:/home";
	}
}