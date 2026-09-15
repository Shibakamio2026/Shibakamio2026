package com.example.shibakamio2026.controller;

import java.util.List;

import jakarta.servlet.http.HttpSession;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.example.shibakamio2026.dto.CsvImportBatch;
import com.example.shibakamio2026.dto.CsvImportRow;
import com.example.shibakamio2026.entity.Asset;
import com.example.shibakamio2026.entity.User;
import com.example.shibakamio2026.repository.AssetRepository;
import com.example.shibakamio2026.repository.CategoryRepository;
import com.example.shibakamio2026.repository.UserRepository;
import com.example.shibakamio2026.service.PaymentImportService;

@Controller
@RequestMapping("/payments/import")
public class PaymentImportController {

	private static final String SESSION_ROWS = "csvImportRows";
	private static final String SESSION_ASSET_ID = "csvImportAssetId";

	private final UserRepository userRepository;
	private final AssetRepository assetRepository;
	private final CategoryRepository categoryRepository;
	private final PaymentImportService paymentImportService;

	public PaymentImportController(UserRepository userRepository,
			AssetRepository assetRepository,
			CategoryRepository categoryRepository,
			PaymentImportService paymentImportService) {
		this.userRepository = userRepository;
		this.assetRepository = assetRepository;
		this.categoryRepository = categoryRepository;
		this.paymentImportService = paymentImportService;
	}

	private User currentUser(Authentication authentication) {
		return userRepository.findByEmailIgnoreCase(authentication.getName())
				.orElseThrow(() -> new IllegalStateException("ユーザーが見つかりません"));
	}

	@GetMapping
	public String form(Authentication authentication, Model model) {
		User user = currentUser(authentication);
		model.addAttribute("assets", assetRepository.findByUserAndIsActiveTrueOrderByAssetIdAsc(user));
		return "payments/import-form";
	}

	@PostMapping("/preview")
	public String preview(@RequestParam("assetId") Long assetId,
			@RequestParam("file") MultipartFile file,
			Authentication authentication,
			HttpSession session,
			Model model) {

		User user = currentUser(authentication);

		if (file.isEmpty()) {
			model.addAttribute("errorMessage", "CSVファイルを選択してください。");
			model.addAttribute("assets", assetRepository.findByUserAndIsActiveTrueOrderByAssetIdAsc(user));
			return "payments/import-form";
		}

		try {
			List<CsvImportRow> rows = paymentImportService.buildPreview(file, user, assetId);
			session.setAttribute(SESSION_ROWS, rows);
			session.setAttribute(SESSION_ASSET_ID, assetId);

			CsvImportBatch batch = new CsvImportBatch();
			batch.setRows(rows);

			model.addAttribute("csvImportBatch", batch);
			model.addAttribute("categories", categoryRepository.findByUserAndIsActiveTrueOrderByCategoryIdAsc(user));
			model.addAttribute("assets", assetRepository.findByUserAndIsActiveTrueOrderByAssetIdAsc(user));
			return "payments/import-preview";
		} catch (Exception e) {
			model.addAttribute("errorMessage", "CSVの読み込みに失敗しました: " + e.getMessage());
			model.addAttribute("assets", assetRepository.findByUserAndIsActiveTrueOrderByAssetIdAsc(user));
			return "payments/import-form";
		}
	}

	@PostMapping("/confirm")
	public String confirm(@ModelAttribute CsvImportBatch csvImportBatch,
			Authentication authentication,
			HttpSession session,
			Model model) {

		User user = currentUser(authentication);

		@SuppressWarnings("unchecked")
		List<CsvImportRow> originalRows = (List<CsvImportRow>) session.getAttribute(SESSION_ROWS);
		Long assetId = (Long) session.getAttribute(SESSION_ASSET_ID);

		if (originalRows == null || assetId == null) {
			return "redirect:/payments/import";
		}

		// 画面で編集された checked / categoryId / targetAssetId / sourceAssetId をマージ
		List<CsvImportRow> editedRows = csvImportBatch.getRows();
		for (int i = 0; i < originalRows.size() && i < editedRows.size(); i++) {
			CsvImportRow original = originalRows.get(i);
			CsvImportRow edited = editedRows.get(i);
			original.setChecked(edited.isChecked());
			original.setCategoryId(edited.getCategoryId());
			original.setTargetAssetId(edited.getTargetAssetId());
			original.setSourceAssetId(edited.getSourceAssetId());
		}

		boolean missingSelection = originalRows.stream().anyMatch(r -> r.isChecked() &&
				("CHARGE".equals(r.getCandidateType())
						? r.getSourceAssetId() == null
						: (r.getCategoryId() == null || r.getTargetAssetId() == null)));

		if (missingSelection) {
			CsvImportBatch batchForRedisplay = new CsvImportBatch();
			batchForRedisplay.setRows(originalRows);

			model.addAttribute("errorMessage", "カテゴリー・登録先資産・振替元資産が未選択の行があります。すべて選択してください。");
			model.addAttribute("csvImportBatch", batchForRedisplay);
			model.addAttribute("categories", categoryRepository.findByUserAndIsActiveTrueOrderByCategoryIdAsc(user));
			model.addAttribute("assets", assetRepository.findByUserAndIsActiveTrueOrderByAssetIdAsc(user));
			return "payments/import-preview";
		}

		Asset chargeToAsset = assetRepository.findById(assetId)
				.orElseThrow(() -> new IllegalArgumentException("資産が見つかりません"));

		paymentImportService.importRows(originalRows, user, chargeToAsset);

		session.removeAttribute(SESSION_ROWS);
		session.removeAttribute(SESSION_ASSET_ID);

		return "redirect:/transactions/list";
	}
}