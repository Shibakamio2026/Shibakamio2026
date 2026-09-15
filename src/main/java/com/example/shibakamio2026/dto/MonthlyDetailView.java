package com.example.shibakamio2026.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** G14 月々のグラフ表示 で、選択された1か月分の詳細を保持するビュー。 */
@Getter
@AllArgsConstructor
public class MonthlyDetailView {
	private MonthlySummary summary;
	private List<CategorySummary> categories;
	private List<AssetBalanceSnapshot> assetBalances;
}
