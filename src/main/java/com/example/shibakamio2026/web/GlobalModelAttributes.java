package com.example.shibakamio2026.web;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.example.shibakamio2026.entity.User;
import com.example.shibakamio2026.repository.UserRepository;

/**
 * 全画面の共通レイアウトで使うモデル属性をまとめて渡す。
 *
 * ヘッダーのログイン判定は以前 sec:authorize に頼っていたが、
 * Spring Security のバージョンと thymeleaf-extras のバージョンが噛み合わないと
 * 黙って非表示になってしまうため、通常のモデル属性に置き換えている。
 */
@ControllerAdvice
public class GlobalModelAttributes {

	private final UserRepository userRepository;

	public GlobalModelAttributes(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	/** ログイン中なら表示名、未ログインなら null。null かどうかでヘッダーの出し分けをする。 */
	@ModelAttribute("currentUserName")
	public String currentUserName(Authentication authentication) {
		if (authentication == null
				|| !authentication.isAuthenticated()
				|| authentication instanceof AnonymousAuthenticationToken) {
			return null;
		}
		return userRepository.findByEmailIgnoreCase(authentication.getName())
				.map(User::getUserName)
				.orElse(authentication.getName());
	}
}