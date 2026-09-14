package com.example.shibakamio2026.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** 分析・レポート画面で表示する、資産ごとの現在残高（要件7.2「資産残高の推移」の簡易版）。 */
@Getter
@AllArgsConstructor
public class AssetBalanceSnapshot {
	private String assetName;
	private BigDecimal currentBalance;
}
