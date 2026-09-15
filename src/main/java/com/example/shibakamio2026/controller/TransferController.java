package com.example.shibakamio2026.controller;

import java.util.Map;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.shibakamio2026.dto.TransferForm;
import com.example.shibakamio2026.service.TransferService;
import com.example.shibakamio2026.web.CurrentUserService;

import lombok.RequiredArgsConstructor;

/**
 * G15 資産間振替 のコントローラ。
 */
@Controller
@RequestMapping("/assets/transfer")
@RequiredArgsConstructor
public class TransferController {

	private final TransferService transferService;
	private final CurrentUserService currentUserService;

	@GetMapping
	public String form(HttpSession session, Model model) {
		Long userId = currentUserService.getCurrentUserId(session);
		if (!model.containsAttribute("transferForm")) {
			model.addAttribute("transferForm", new TransferForm());
		}
		model.addAttribute("assets", transferService.getSelectableAssets(userId));
		return "assets/transfer";
	}

	@PostMapping
	public String submit(@ModelAttribute TransferForm transferForm,
			HttpSession session,
			Model model,
			RedirectAttributes redirectAttributes) {
		Long userId = currentUserService.getCurrentUserId(session);

		Map<String, String> errors = transferService.validate(userId, transferForm);
		if (!errors.isEmpty()) {
			model.addAttribute("errors", errors);
			model.addAttribute("assets", transferService.getSelectableAssets(userId));
			return "assets/transfer";
		}

		transferService.createTransfer(userId, transferForm);
		redirectAttributes.addFlashAttribute("successMessage", "資産間振替を登録しました");
		return "redirect:/assets";
	}
}
