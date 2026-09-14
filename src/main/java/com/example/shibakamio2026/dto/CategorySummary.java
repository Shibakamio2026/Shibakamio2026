package com.example.shibakamio2026.dto;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

/**
 * カテゴリー別の収支集計（要件7.2）。確定済みの収支のみを対象とする。
 */
@Getter
@Setter
public class CategorySummary {

	private String categoryName;

	/** "INCOME" または "EXPENSE" */
	private String transactionType;

	private BigDecimal amount;

	/** 棒グラフ表示用：カテゴリー内の最大値に対する割合（0〜100）。ReportServiceで設定する。 */
	private double barPercent;
}
