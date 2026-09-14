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

import com.example.shibakamio2026.dto.AccountDeleteForm;
import com.example.shibakamio2026.dto.AccountForm;
import com.example.shibakamio2026.entity.User;
import com.example.shibakamio2026.service.AccountService;
import com.example.shibakamio2026.web.CurrentUserService;

import lombok.RequiredArgsConstructor;

/**
 * G12 アカウント管理 / G17 アカウント削除 のコントローラ。
 */
@Controller
@RequestMapping("/settings/account")
@RequiredArgsConstructor
public class AccountController {

	private final AccountService accountService;
	private final CurrentUserService currentUserService;

	// ------------------------------------------------------------
	// G12 アカウント管理
	// ------------------------------------------------------------

	@GetMapping
	public String form(HttpSession session, Model model) {
		Long userId = currentUserService.getCurrentUserId(session);
		if (!model.containsAttribute("accountForm")) {
			User user = accountService.getUser(userId);
			model.addAttribute("accountForm", accountService.toForm(user));
		}
		return "settings/account";
	}

	@PostMapping
	public String update(@ModelAttribute AccountForm accountForm,
			HttpSession session,
			Model model,
			RedirectAttributes redirectAttributes) {
		Long userId = currentUserService.getCurrentUserId(session);

		Map<String, String> errors = accountService.validateAccountForm(userId, accountForm);
		if (!errors.isEmpty()) {
			model.addAttribute("errors", errors);
			return "settings/account";
		}

		accountService.updateAccount(userId, accountForm);
		redirectAttributes.addFlashAttribute("successMessage", "アカウント情報を更新しました");
		return "redirect:/settings/account";
	}

	// ------------------------------------------------------------
	// G17 アカウント削除
	// ------------------------------------------------------------

	@GetMapping("/delete")
	public String deleteForm(Model model) {
		if (!model.containsAttribute("accountDeleteForm")) {
			model.addAttribute("accountDeleteForm", new AccountDeleteForm());
		}
		return "settings/account-delete";
	}

	@PostMapping("/delete")
	public String delete(@ModelAttribute AccountDeleteForm accountDeleteForm,
			HttpSession session,
			Model model) {
		Long userId = currentUserService.getCurrentUserId(session);

		String passwordError = accountService.validateCurrentPassword(userId, accountDeleteForm.getCurrentPassword());
		if (!passwordError.isEmpty()) {
			model.addAttribute("errors", Map.of("currentPassword", passwordError));
			return "settings/account-delete";
		}

		accountService.deleteAccount(userId);

		// アカウント削除後、対象ユーザーがログインできない状態にする（要件10.5）
		session.invalidate();
		return "redirect:/login";
	}
}
