package com.example.shibakamio2026.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DashboardMonthPoint {

	/** 表示用ラベル（例："9月"） */
	private String label;

	/** その月の確定収入 */
	private BigDecimal income;

	/** その月の確定支出 */
	private BigDecimal expense;

	/** その月末時点の総資産額 */
	private BigDecimal endBalance;

	/** 棒の高さ（0〜100）。収入・支出は6か月間の最大値を100とする */
	private double incomePercent;

	private double expensePercent;

	/** 棒の高さ（0〜100）。月末残高の最大値を100とする */
	private double balancePercent;
}