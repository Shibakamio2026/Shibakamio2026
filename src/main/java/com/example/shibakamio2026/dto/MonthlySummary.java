package com.example.shibakamio2026.dto;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

/**
 * 月ごとの収支サマリー。確定済みの収支と、未確定（予定・確認待ち）の予定収支を区別して保持する（要件7.2, 7.3）。
 */
@Getter
@Setter
public class MonthlySummary {

	/** "yyyy-MM" 形式 */
	private String yearMonth;

	private BigDecimal confirmedIncome = BigDecimal.ZERO;
	private BigDecimal confirmedExpense = BigDecimal.ZERO;

	private BigDecimal scheduledIncome = BigDecimal.ZERO;
	private BigDecimal scheduledExpense = BigDecimal.ZERO;

	/** 確定済みの収支差額 = confirmedIncome - confirmedExpense */
	public BigDecimal getConfirmedDifference() {
		return confirmedIncome.subtract(confirmedExpense);
	}

	/** 棒グラフ表示用：この月の最大値に対する割合（0〜100）。ReportServiceで設定する。 */
	private double incomeBarPercent;
	private double expenseBarPercent;
}
