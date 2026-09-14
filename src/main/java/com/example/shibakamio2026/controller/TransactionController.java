package com.example.shibakamio2026.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.shibakamio2026.dto.TransactionEditForm;
import com.example.shibakamio2026.dto.TransactionForm;
import com.example.shibakamio2026.entity.Asset;
import com.example.shibakamio2026.entity.AutoTransaction;
import com.example.shibakamio2026.entity.Category;
import com.example.shibakamio2026.entity.Transaction;
import com.example.shibakamio2026.entity.User;
import com.example.shibakamio2026.enums.TransactionType;
import com.example.shibakamio2026.repository.AssetRepository;
import com.example.shibakamio2026.repository.CategoryRepository;
import com.example.shibakamio2026.repository.UserRepository;
import com.example.shibakamio2026.service.AutoTransactionService;
import com.example.shibakamio2026.service.TransactionService;

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
	// (前回渡した inputForm / submit はそのまま)

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

	// ===== G18 収支一覧 =====

	@GetMapping("/list")
	public String list(Authentication authentication,
			@RequestParam(required = false) LocalDate from,
			@RequestParam(required = false) LocalDate to,
			@RequestParam(required = false) Long categoryId,
			@RequestParam(required = false) Long assetId,
			@RequestParam(required = false) TransactionType transactionType,
			Model model) {

		User user = currentUser(authentication);

		if (from != null && to != null && from.isAfter(to)) {
			model.addAttribute("errorMessage", "正しい期間を入力してください。");
		}

		List<Transaction> transactions = transactionService.findAllForUser(user).stream()
				.filter(t -> from == null || !t.getTransactionDate().isBefore(from))
				.filter(t -> to == null || !t.getTransactionDate().isAfter(to))
				.filter(t -> categoryId == null || t.getCategory().getCategoryId().equals(categoryId))
				.filter(t -> assetId == null || t.getAsset().getAssetId().equals(assetId))
				.filter(t -> transactionType == null || t.getTransactionType() == transactionType)
				.collect(Collectors.toList());

		model.addAttribute("transactions", transactions);
		model.addAttribute("categories", categoryRepository.findByUserAndActiveTrue(user));
		model.addAttribute("assets", assetRepository.findByUserAndActiveTrue(user));
		model.addAttribute("from", from);
		model.addAttribute("to", to);
		model.addAttribute("categoryId", categoryId);
		model.addAttribute("assetId", assetId);
		model.addAttribute("transactionType", transactionType);

		return "transactions/list";
	}

	// ===== G19 収支詳細・編集 =====

	@GetMapping("/{transactionId}/edit")
	public String editForm(@PathVariable Long transactionId, Authentication authentication, Model model) {
		User user = currentUser(authentication);
		Transaction transaction = transactionService.findByIdForUser(transactionId, user);

		TransactionEditForm form = new TransactionEditForm();
		form.setTransactionType(transaction.getTransactionType());
		form.setTransactionDate(transaction.getTransactionDate());
		form.setAmount(transaction.getAmount());
		form.setCategoryId(transaction.getCategory().getCategoryId());
		form.setAssetId(transaction.getAsset().getAssetId());
		form.setMemo(transaction.getMemo());

		model.addAttribute("transactionId", transactionId);
		model.addAttribute("transactionEditForm", form);
		model.addAttribute("categories", categoryRepository.findByUserAndActiveTrue(user));
		model.addAttribute("assets", assetRepository.findByUserAndActiveTrue(user));
		return "transactions/edit";
	}

	@PostMapping("/{transactionId}/edit")
	public String editSubmit(@PathVariable Long transactionId,
			@ModelAttribute("transactionEditForm") TransactionEditForm form,
			Authentication authentication,
			Model model) {

		User user = currentUser(authentication);
		Transaction transaction = transactionService.findByIdForUser(transactionId, user);

		StringBuilder errors = new StringBuilder();

		if (form.getTransactionDate() == null) {
			errors.append("未入力か正しくない日付です。");
		}
		if (form.getCategoryId() == null) {
			errors.append("カテゴリーを選択してください。");
		}
		if (form.getAmount() == null || form.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
			errors.append("未入力か０より多くない金額です。");
		}
		if (form.getAssetId() == null) {
			errors.append("資産を選択してください。");
		}
		if (form.getTransactionType() == null) {
			errors.append("収支種別を選択してください。");
		}

		if (errors.length() > 0) {
			model.addAttribute("errorMessage", errors.toString());
			model.addAttribute("transactionId", transactionId);
			model.addAttribute("categories", categoryRepository.findByUserAndActiveTrue(user));
			model.addAttribute("assets", assetRepository.findByUserAndActiveTrue(user));
			return "transactions/edit";
		}

		Category category = categoryRepository.findById(form.getCategoryId())
				.orElseThrow(() -> new IllegalArgumentException("カテゴリーが見つかりません"));
		Asset asset = assetRepository.findById(form.getAssetId())
				.orElseThrow(() -> new IllegalArgumentException("資産が見つかりません"));

		transaction.setTransactionType(form.getTransactionType());
		transaction.setTransactionDate(form.getTransactionDate());
		transaction.setAmount(form.getAmount());
		transaction.setCategory(category);
		transaction.setAsset(asset);
		transaction.setMemo(form.getMemo());
		transactionService.update(transaction);

		return "redirect:/transactions/list";
	}

	@PostMapping("/{transactionId}/delete")
	public String delete(@PathVariable Long transactionId, Authentication authentication) {
		User user = currentUser(authentication);
		Transaction transaction = transactionService.findByIdForUser(transactionId, user);
		transactionService.delete(transaction);
		return "redirect:/transactions/list";
	}
}