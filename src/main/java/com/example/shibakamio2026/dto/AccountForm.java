package com.example.shibakamio2026.dto;

import lombok.Getter;
import lombok.Setter;

/** G12 アカウント管理 の入力フォーム。パスワード変更は本システムの対象外（要件10.4）。 */
@Getter
@Setter
public class AccountForm {

	private String userName;

	private String email;
}
