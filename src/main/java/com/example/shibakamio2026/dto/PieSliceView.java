package com.example.shibakamio2026.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/** ダッシュボードの支出内訳（円グラフ）1切れ分。 */
@Getter
@Setter
@AllArgsConstructor
public class PieSliceView {

	private String label;

	private BigDecimal amount;

	/** 全体に対する割合（0〜100） */
	private double percent;

	/** 描画色（例："#4caf7d"） */
	private String color;
}