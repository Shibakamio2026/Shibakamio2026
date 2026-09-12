package com.example.demo.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.AutoTransactionService;

@Controller
@RequestMapping("/transactions/auto")
public class AutoTransactionController {

	private final UserRepository userRepository;
	private final AutoTransactionService autoTransactionService;

	public AutoTransactionController(UserRepository userRepository,
			AutoTransactionService autoTransactionService) {
		this.userRepository = userRepository;
		this.autoTransactionService = autoTransactionService;
	}

	private User currentUser(Authentication authentication) {
		return userRepository.findByEmail(authentication.getName())
				.orElseThrow(() -> new IllegalStateException("ユーザーが見つかりません"));
	}

	@GetMapping
	public String list(Authentication authentication, Model model) {
		User user = currentUser(authentication);
		model.addAttribute("autoTransactions", autoTransactionService.findAllForUser(user));
		return "transactions/auto-list";
	}

	@PostMapping("/{id}/disable")
	public String disable(@PathVariable Long id, Authentication authentication) {
		User user = currentUser(authentication);
		autoTransactionService.disable(id, user);
		return "redirect:/transactions/auto";
	}
}