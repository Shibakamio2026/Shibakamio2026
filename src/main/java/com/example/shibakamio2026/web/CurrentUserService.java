package com.example.shibakamio2026.web;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Component;

/**
 * 資産管理モジュール・設定モジュールにあるものと同一内容です。実リポジトリには
 * 1つだけ存在すればよいので、統合時はどちらか一方を残してください。
 *
 * ログイン中ユーザーのIDを取得する共通処理。
 * ログイン成功時（G01）に、セッション属性 "LOGIN_USER_ID" に users.user_id を
 * Long として setAttribute してもらうことを想定しています。
 * 認証が未実装の間は、動作確認のため userId = 1 にフォールバックします。
 */
@Component
public class CurrentUserService {

	public static final String SESSION_KEY_USER_ID = "LOGIN_USER_ID";

	/** TODO: 認証実装後に削除する開発用フォールバック値 */
	private static final Long DEV_FALLBACK_USER_ID = 1L;

	public Long getCurrentUserId(HttpSession session) {
		Object value = session.getAttribute(SESSION_KEY_USER_ID);
		if (value instanceof Long l) {
			return l;
		}
		if (value instanceof Number n) {
			return n.longValue();
		}
		return DEV_FALLBACK_USER_ID;
	}
}
