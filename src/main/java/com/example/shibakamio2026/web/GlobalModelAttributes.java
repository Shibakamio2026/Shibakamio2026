package com.example.shibakamio2026.web;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * 全画面共通のモデル属性。
 *
 * ヘッダーのナビゲーションで sec:authorize / sec:authentication を使わずに済むように、
 * ログイン中のユーザー名をモデルに入れておく。未ログインのときは null。
 * これにより thymeleaf-extras-springsecurity のバージョン差の影響を受けなくなる。
 */
@ControllerAdvice
public class GlobalModelAttributes {

	@ModelAttribute("currentUserName")
	public String currentUserName(Authentication authentication) {
		if (authentication == null
				|| !authentication.isAuthenticated()
				|| authentication instanceof AnonymousAuthenticationToken) {
			return null;
		}
		return authentication.getName();
	}
}