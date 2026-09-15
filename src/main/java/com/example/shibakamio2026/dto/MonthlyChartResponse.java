package com.example.shibakamio2026.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * グラフ表示用データのJSON出力（要件8章）。
 * ログイン中ユーザー自身のデータのみを出力する（8.3）。集計対象期間・月ごとの収入・支出・
 * カテゴリー別集計・資産残高を含む。
 */
@Getter
@AllArgsConstructor
public class MonthlyChartResponse {

	/** 集計対象期間（開始月, "yyyy-MM"） */
	private String periodFrom;

	/** 集計対象期間（終了月, "yyyy-MM"） */
	private String periodTo;

	private List<MonthlySummary> months;

	private List<CategorySummary> categories;

	private List<AssetBalanceSnapshot> assetBalances;
}
