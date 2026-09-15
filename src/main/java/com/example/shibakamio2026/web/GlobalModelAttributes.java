package com.example.shibakamio2026.web;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.example.shibakamio2026.entity.User;
import com.example.shibakamio2026.repository.UserRepository;

/**
 * 共通レイアウト（ヘッダー）で使うモデル属性を全画面に渡す。
 *
 * ログイン判定は以前 sec:authorize に頼っていたが、Spring Security と
 * thymeleaf-extras のバージョンが噛み合わないと黙って非表示になるため、
 * 通常のモデル属性に置き換えている。
 *
 * Authentication をメソッド引数で受け取らず SecurityContextHolder から直接読むのは、
 * @ControllerAdvice の @ModelAttribute では引数解決の経路が通常のコントローラーと
 * 異なる場合があり、null が入り込む余地をなくすため。
 */
@ControllerAdvice
public class GlobalModelAttributes {

	private final UserRepository userRepository;

	public GlobalModelAttributes(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	/** ログイン中の表示名。取得できない場合は空文字（ヘッダー自体は常に表示する）。 */
	@ModelAttribute("currentUserName")
	public String currentUserName() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null
				|| !authentication.isAuthenticated()
				|| authentication instanceof AnonymousAuthenticationToken) {
			return "";
		}

		String email = authentication.getName();
		return userRepository.findByEmailIgnoreCase(email)
				.map(User::getUserName)
				.orElse(email);
	}
}