package com.example.shibakamio2026.controller;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.shibakamio2026.dto.LoginForm;
import com.example.shibakamio2026.dto.SignupForm;
import com.example.shibakamio2026.service.AccountService;

@Controller
public class AuthController {

	private final AccountService accountService;

	public AuthController(AccountService accountService) {
		this.accountService = accountService;
	}

	@GetMapping("/")
	public String root() {
		return "redirect:/login";
	}

	// G01 ログイン画面（G02のエラー表示もこの画面が兼ねる）
	@GetMapping("/login")
	public String loginPage(Model model) {
		model.addAttribute("loginForm", new LoginForm());
		return "login";
	}

	// G16 新規アカウント登録画面
	@GetMapping("/signup")
	public String signupForm(Model model) {
		model.addAttribute("signupForm", new SignupForm());
		return "signup";
	}

	@PostMapping("/signup")
	public String signup(@Valid @ModelAttribute("signupForm") SignupForm signupForm,
			BindingResult bindingResult) {

		if (signupForm.getPassword() != null
				&& !signupForm.getPassword().equals(signupForm.getConfirmPassword())) {
			bindingResult.rejectValue("confirmPassword", "password.mismatch", "パスワードの一致がしていません");
		}

		if (signupForm.getEmail() != null && accountService.isEmailTaken(signupForm.getEmail())) {
			bindingResult.rejectValue("email", "email.duplicate", "このメールアドレスは既に登録されています");
		}

		if (bindingResult.hasErrors()) {
			return "signup";
		}

		accountService.registerUser(signupForm);
		return "redirect:/login";
	}
}