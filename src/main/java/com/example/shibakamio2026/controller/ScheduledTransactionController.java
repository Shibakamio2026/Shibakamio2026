package com.example.shibakamio2026.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.shibakamio2026.entity.User;
import com.example.shibakamio2026.repository.UserRepository;
import com.example.shibakamio2026.service.ScheduledTransactionService;

@Controller
@RequestMapping("/transactions/scheduled")
public class ScheduledTransactionController {

	private final UserRepository userRepository;
	private final ScheduledTransactionService scheduledTransactionService;

	public ScheduledTransactionController(UserRepository userRepository,
			ScheduledTransactionService scheduledTransactionService) {
		this.userRepository = userRepository;
		this.scheduledTransactionService = scheduledTransactionService;
	}

	private User currentUser(Authentication authentication) {
		return userRepository.findByEmailIgnoreCase(authentication.getName())
				.orElseThrow(() -> new IllegalStateException("ユーザーが見つかりません"));
	}

	@GetMapping
	public String list(Authentication authentication, Model model) {
		User user = currentUser(authentication);
		model.addAttribute("scheduledTransactions", scheduledTransactionService.findPendingForUser(user));
		return "transactions/scheduled-list";
	}

	@PostMapping("/{id}/confirm")
	public String confirm(@PathVariable Long id, Authentication authentication) {
		User user = currentUser(authentication);
		scheduledTransactionService.confirm(id, user);
		return "redirect:/transactions/scheduled";
	}

	@PostMapping("/{id}/cancel")
	public String cancel(@PathVariable Long id, Authentication authentication) {
		User user = currentUser(authentication);
		scheduledTransactionService.cancel(id, user);
		return "redirect:/transactions/scheduled";
	}
}