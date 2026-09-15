package com.example.shibakamio2026.service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.shibakamio2026.dto.AccountForm;
import com.example.shibakamio2026.entity.User;
import com.example.shibakamio2026.exception.ResourceNotFoundException;
import com.example.shibakamio2026.repository.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * アカウント管理・削除のサービスクラス。G12 アカウント管理 / G17 アカウント削除 に対応する。
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountService {

	private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	@PersistenceContext
	private EntityManager entityManager;

	// ------------------------------------------------------------
	// G12 アカウント管理
	// ------------------------------------------------------------

	public User getUser(Long userId) {
		return userRepository.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("ユーザーが見つかりません"));
	}

	public AccountForm toForm(User user) {
		AccountForm form = new AccountForm();
		form.setUserName(user.getUserName());
		form.setEmail(user.getEmail());
		return form;
	}

	public Map<String, String> validateAccountForm(Long userId, AccountForm form) {
		Map<String, String> errors = new LinkedHashMap<>();

		if (isBlank(form.getUserName())) {
			errors.put("userName", "入力してください");
		}

		String email = form.getEmail();
		boolean blankOrInvalid = isBlank(email) || !EMAIL_PATTERN.matcher(email.trim()).matches();
		if (blankOrInvalid) {
			errors.put("email", "未入力もしくは正しくないメールアドレスです");
		} else if (userRepository.existsByEmailIgnoreCaseAndUserIdNot(email.trim(), userId)) {
			errors.put("email", "このメールアドレスは既に使用されています");
		}

		return errors;
	}

	@Transactional
	public void updateAccount(Long userId, AccountForm form) {
		User user = getUser(userId);
		user.setUserName(form.getUserName().trim());
		user.setEmail(form.getEmail().trim());
		userRepository.save(user);
	}

	// ------------------------------------------------------------
	// G17 アカウント削除
	// ------------------------------------------------------------

	/** 入力されたパスワードが正しいか確認する。誤っていればエラーメッセージを返す（正しければ空文字）。 */
	public String validateCurrentPassword(Long userId, String rawPassword) {
		if (isBlank(rawPassword)) {
			return "パスワードを入力してください";
		}
		User user = getUser(userId);
		if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
			return "パスワードが正しくありません";
		}
		return "";
	}

	/**
	 * アカウントおよび関連データ（収支・自動収支・予定収支・カテゴリー・資産・資産間振替）を削除する（要件10.5）。
	 * transactions / auto_transactions / scheduled_transactions は他モジュール担当のテーブルのため、
	 * ネイティブSQLで直接削除する。外部キー制約の依存関係に従い、子テーブルから順に削除する。
	 */
	@Transactional
	public void deleteAccount(Long userId) {
		// 1. 収支実績（scheduled_transaction_id で予定収支を参照しているため先に削除）
		entityManager.createNativeQuery(
				"DELETE FROM transactions WHERE asset_id IN (SELECT asset_id FROM assets WHERE user_id = :userId)")
				.setParameter("userId", userId).executeUpdate();

		// 2. 資産間振替
		entityManager.createNativeQuery(
				"DELETE FROM transfers WHERE from_asset_id IN (SELECT asset_id FROM assets WHERE user_id = :userId) " +
						"OR to_asset_id IN (SELECT asset_id FROM assets WHERE user_id = :userId)")
				.setParameter("userId", userId).executeUpdate();

		// 3. 予定収支（auto_transaction_id で自動収支を参照しているため自動収支より先に削除）
		entityManager.createNativeQuery(
				"DELETE FROM scheduled_transactions WHERE asset_id IN (SELECT asset_id FROM assets WHERE user_id = :userId)")
				.setParameter("userId", userId).executeUpdate();

		// 4. 自動収支
		entityManager.createNativeQuery(
				"DELETE FROM auto_transactions WHERE asset_id IN (SELECT asset_id FROM assets WHERE user_id = :userId)")
				.setParameter("userId", userId).executeUpdate();

		// 5. カテゴリー
		entityManager.createNativeQuery(
				"DELETE FROM categories WHERE user_id = :userId").setParameter("userId", userId).executeUpdate();

		// 6. 資産
		entityManager.createNativeQuery(
				"DELETE FROM assets WHERE user_id = :userId").setParameter("userId", userId).executeUpdate();

		// 7. ユーザー本体
		entityManager.createNativeQuery(
				"DELETE FROM users WHERE user_id = :userId").setParameter("userId", userId).executeUpdate();
	}

	private boolean isBlank(String s) {
		return s == null || s.isBlank();
	}
}