package com.example.shibakamio2026.service;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.shibakamio2026.dto.CategoryForm;
import com.example.shibakamio2026.dto.CategoryView;
import com.example.shibakamio2026.entity.Category;
import com.example.shibakamio2026.entity.User;
import com.example.shibakamio2026.exception.ResourceNotFoundException;
import com.example.shibakamio2026.repository.CategoryRepository;
import com.example.shibakamio2026.repository.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * カテゴリー管理のサービスクラス。G10 カテゴリー登録 に対応する。
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

	private static final String DUPLICATE_OR_BLANK_MESSAGE = "未入力もしくはすでに存在するカテゴリー名の入力があります";

	private final CategoryRepository categoryRepository;
	private final UserRepository userRepository;

	/** ログイン中ユーザーのカテゴリーを、使用中フラグ付きで一覧取得する（無効化済みも含む）。 */
	public java.util.List<CategoryView> getCategories(Long userId) {
		return categoryRepository.findByUser_UserIdOrderByCategoryIdAsc(userId).stream()
				.map(c -> new CategoryView(
						c.getCategoryId(),
						c.getCategoryName(),
						Boolean.TRUE.equals(c.getIsActive()),
						isUsed(c.getCategoryId())))
				.toList();
	}

	private boolean isUsed(Long categoryId) {
		return categoryRepository.countUsageInTransactions(categoryId) > 0
				|| categoryRepository.countUsageInAutoTransactions(categoryId) > 0
				|| categoryRepository.countUsageInScheduledTransactions(categoryId) > 0;
	}

	public Category getOwnedCategory(Long userId, Long categoryId) {
		return categoryRepository.findByCategoryIdAndUser_UserId(categoryId, userId)
				.orElseThrow(() -> new ResourceNotFoundException("カテゴリーが見つかりません"));
	}

	public Map<String, String> validateCreate(Long userId, CategoryForm form) {
		Map<String, String> errors = new LinkedHashMap<>();
		String name = trim(form.getCategoryName());
		boolean blank = name.isEmpty();
		boolean duplicate = !blank && categoryRepository.existsByUser_UserIdAndCategoryNameIgnoreCase(userId, name);
		if (blank || duplicate) {
			errors.put("categoryName", DUPLICATE_OR_BLANK_MESSAGE);
		}
		return errors;
	}

	public Map<String, String> validateEdit(Long userId, Long categoryId, CategoryForm form) {
		Map<String, String> errors = new LinkedHashMap<>();
		String name = trim(form.getCategoryName());
		boolean blank = name.isEmpty();
		boolean duplicate = !blank && categoryRepository
				.existsByUser_UserIdAndCategoryNameIgnoreCaseAndCategoryIdNot(userId, name, categoryId);
		if (blank || duplicate) {
			errors.put("categoryName", DUPLICATE_OR_BLANK_MESSAGE);
		}
		return errors;
	}

	@Transactional
	public void createCategory(Long userId, CategoryForm form) {
		User user = userRepository.getReferenceById(userId);
		Category category = new Category();
		category.setUser(user);
		category.setCategoryName(trim(form.getCategoryName()));
		category.setIsActive(true);
		categoryRepository.save(category);
	}

	@Transactional
	public void updateCategory(Long userId, Long categoryId, CategoryForm form) {
		Category category = getOwnedCategory(userId, categoryId);
		category.setCategoryName(trim(form.getCategoryName()));
		categoryRepository.save(category);
	}

	/** 使用中のカテゴリーは削除せず無効化する（要件6.3）。無効化後は新規収支登録時に選択できない。 */
	@Transactional
	public void deactivateCategory(Long userId, Long categoryId) {
		Category category = getOwnedCategory(userId, categoryId);
		category.setIsActive(false);
		categoryRepository.save(category);
	}

	/** 未使用のカテゴリーのみ削除できる。使用中の場合は例外を投げる。 */
	@Transactional
	public void deleteCategory(Long userId, Long categoryId) {
		Category category = getOwnedCategory(userId, categoryId);
		if (isUsed(categoryId)) {
			throw new IllegalStateException("使用中のカテゴリーは削除できません");
		}
		categoryRepository.delete(category);
	}

	private String trim(String s) {
		return s == null ? "" : s.trim();
	}
}
