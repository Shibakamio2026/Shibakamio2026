package com.example.shibakamio2026.dto;

import lombok.Getter;
import lombok.Setter;

/** G17 アカウント削除 の入力フォーム。本人確認のため現在のパスワードを入力させる。 */
@Getter
@Setter
public class AccountDeleteForm {

	private String currentPassword;
}
