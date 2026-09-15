package com.example.shibakamio2026.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DashboardCategorySlice {

	/** カテゴリー名（6件目以降はまとめて "その他"） */
	private String name;

	private BigDecimal amount;

	/** 支出合計に対する割合（0〜100） */
	private double percent;

	/** 円グラフ・凡例で使う色（CSSの色文字列） */
	private String color;
}