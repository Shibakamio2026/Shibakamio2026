package com.example.shibakamio2026.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** G10 カテゴリー登録 の一覧表示用データ。使用中かどうかで削除ボタンの表示可否を切り替える。 */
@Getter
@AllArgsConstructor
public class CategoryView {

	private Long categoryId;
	private String categoryName;
	private boolean active;
	/** transactions / auto_transactions / scheduled_transactions のいずれかで使用されているか */
	private boolean used;
}