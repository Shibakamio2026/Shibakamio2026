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

import com.example.shibakamio2026.dto.CategoryForm;
import com.example.shibakamio2026.service.CategoryService;
import com.example.shibakamio2026.web.CurrentUserService;

import lombok.RequiredArgsConstructor;

/**
 * G10 カテゴリー登録 のコントローラ。
 */
@Controller
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

	private final CategoryService categoryService;
	private final CurrentUserService currentUserService;

	@GetMapping
	public String list(HttpSession session, Model model) {
		Long userId = currentUserService.getCurrentUserId(session);
		model.addAttribute("categories", categoryService.getCategories(userId));
		if (!model.containsAttribute("categoryForm")) {
			model.addAttribute("categoryForm", new CategoryForm());
		}
		return "categories/list";
	}

	@PostMapping
	public String create(@ModelAttribute CategoryForm categoryForm,
			HttpSession session,
			Model model,
			RedirectAttributes redirectAttributes) {
		Long userId = currentUserService.getCurrentUserId(session);
		Map<String, String> errors = categoryService.validateCreate(userId, categoryForm);
		if (!errors.isEmpty()) {
			model.addAttribute("errors", errors);
			model.addAttribute("categories", categoryService.getCategories(userId));
			return "categories/list";
		}

		categoryService.createCategory(userId, categoryForm);
		redirectAttributes.addFlashAttribute("successMessage", "カテゴリーを登録しました");
		return "redirect:/categories";
	}

	@PostMapping("/{categoryId}/edit")
	public String edit(@PathVariable Long categoryId,
			@ModelAttribute CategoryForm categoryForm,
			HttpSession session,
			Model model,
			RedirectAttributes redirectAttributes) {
		Long userId = currentUserService.getCurrentUserId(session);
		// 存在確認・所有者確認
		categoryService.getOwnedCategory(userId, categoryId);

		Map<String, String> errors = categoryService.validateEdit(userId, categoryId, categoryForm);
		if (!errors.isEmpty()) {
			model.addAttribute("categories", categoryService.getCategories(userId));
			model.addAttribute("categoryForm", new CategoryForm());
			model.addAttribute("errors", errors);
			model.addAttribute("editingCategoryId", categoryId);
			return "categories/list";
		}

		categoryService.updateCategory(userId, categoryId, categoryForm);
		redirectAttributes.addFlashAttribute("successMessage", "カテゴリーを更新しました");
		return "redirect:/categories";
	}

	@PostMapping("/{categoryId}/deactivate")
	public String deactivate(@PathVariable Long categoryId, HttpSession session,
			RedirectAttributes redirectAttributes) {
		Long userId = currentUserService.getCurrentUserId(session);
		categoryService.deactivateCategory(userId, categoryId);
		redirectAttributes.addFlashAttribute("successMessage", "カテゴリーを無効化しました");
		return "redirect:/categories";
	}

	@PostMapping("/{categoryId}/delete")
	public String delete(@PathVariable Long categoryId, HttpSession session,
			RedirectAttributes redirectAttributes) {
		Long userId = currentUserService.getCurrentUserId(session);
		try {
			categoryService.deleteCategory(userId, categoryId);
			redirectAttributes.addFlashAttribute("successMessage", "カテゴリーを削除しました");
		} catch (IllegalStateException e) {
			redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
		}
		return "redirect:/categories";
	}
}
