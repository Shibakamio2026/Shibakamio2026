package com.example.shibakamio2026.controller;

import java.util.Map;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.shibakamio2026.dto.AssetCreateForm;
import com.example.shibakamio2026.dto.AssetEditForm;
import com.example.shibakamio2026.entity.Asset;
import com.example.shibakamio2026.enums.AssetType;
import com.example.shibakamio2026.service.AssetService;
import com.example.shibakamio2026.web.CurrentUserService;

import lombok.RequiredArgsConstructor;

/**
 * G05 口座・資産一覧 / G13 資産更新 / G21 資産登録 のコントローラ。
 */
@Controller
@RequestMapping("/assets")
@RequiredArgsConstructor
public class AssetController {

	private final AssetService assetService;
	private final CurrentUserService currentUserService;

	// ------------------------------------------------------------
	// G05 口座・資産一覧
	// ------------------------------------------------------------

	@GetMapping
	public String list(HttpSession session, Model model) {
		Long userId = currentUserService.getCurrentUserId(session);
		var balances = assetService.getAssetBalances(userId);

		model.addAttribute("assets", balances);
		// 要件：表示対象の資産が1件も存在しない場合は「項目を追加してから利用できます」と案内する
		model.addAttribute("isEmpty", balances.isEmpty());
		return "assets/list";
	}

	// ------------------------------------------------------------
	// G21 資産登録
	// ------------------------------------------------------------

	@GetMapping("/new")
	public String newForm(Model model) {
		if (!model.containsAttribute("assetCreateForm")) {
			model.addAttribute("assetCreateForm", new AssetCreateForm());
		}
		model.addAttribute("assetTypes", AssetType.values());
		return "assets/new";
	}

	@PostMapping("/new")
	public String create(@ModelAttribute AssetCreateForm assetCreateForm,
			HttpSession session,
			Model model,
			RedirectAttributes redirectAttributes) {
		Map<String, String> errors = assetService.validateCreateForm(assetCreateForm);
		if (!errors.isEmpty()) {
			model.addAttribute("errors", errors);
			model.addAttribute("assetTypes", AssetType.values());
			return "assets/new";
		}

		Long userId = currentUserService.getCurrentUserId(session);
		assetService.createAsset(userId, assetCreateForm);

		redirectAttributes.addFlashAttribute("successMessage", "資産を登録しました");
		return "redirect:/assets";
	}

	// ------------------------------------------------------------
	// G13 資産更新
	// ------------------------------------------------------------

	@GetMapping("/{assetId}/edit")
	public String editForm(@PathVariable Long assetId, HttpSession session, Model model) {
		Long userId = currentUserService.getCurrentUserId(session);
		Asset asset = assetService.getOwnedAsset(userId, assetId);

		if (!model.containsAttribute("assetEditForm")) {
			AssetEditForm form = new AssetEditForm();
			form.setAssetName(asset.getAssetName());
			form.setAssetType(asset.getAssetType());
			model.addAttribute("assetEditForm", form);
		}
		model.addAttribute("assetId", assetId);
		model.addAttribute("assetTypes", AssetType.values());
		return "assets/edit";
	}

	@PostMapping("/{assetId}/edit")
	public String update(@PathVariable Long assetId,
			@ModelAttribute AssetEditForm assetEditForm,
			HttpSession session,
			Model model,
			RedirectAttributes redirectAttributes) {
		Long userId = currentUserService.getCurrentUserId(session);
		// 存在確認・所有者確認（他ユーザーの資産は編集できない）
		assetService.getOwnedAsset(userId, assetId);

		Map<String, String> errors = assetService.validateEditForm(assetEditForm);
		if (!errors.isEmpty()) {
			model.addAttribute("errors", errors);
			model.addAttribute("assetId", assetId);
			model.addAttribute("assetTypes", AssetType.values());
			return "assets/edit";
		}

		assetService.updateAsset(userId, assetId, assetEditForm);
		redirectAttributes.addFlashAttribute("successMessage", "資産情報を更新しました");
		return "redirect:/assets";
	}

	@PostMapping("/{assetId}/deactivate")
	public String deactivate(@PathVariable Long assetId, HttpSession session, RedirectAttributes redirectAttributes) {
		Long userId = currentUserService.getCurrentUserId(session);
		assetService.deactivateAsset(userId, assetId);
		redirectAttributes.addFlashAttribute("successMessage", "資産を無効化しました");
		return "redirect:/assets";
	}
}
