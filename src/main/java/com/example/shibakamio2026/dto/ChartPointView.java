package com.example.shibakamio2026.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/** ダッシュボードの棒グラフ1本分（ラベル・値・棒の高さ%）。 */
@Getter
@Setter
@AllArgsConstructor
public class ChartPointView {

	/** 軸ラベル（例："9月"） */
	private String label;

	private BigDecimal value;

	/** 棒の高さ（0〜100）。グラフ内の最大値を100とする。 */
	private double percent;
}