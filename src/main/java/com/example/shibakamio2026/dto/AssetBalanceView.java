package com.example.shibakamio2026.dto;

import java.math.BigDecimal;

import com.example.shibakamio2026.enums.AssetType;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * G05 口座・資産一覧 の表示用データ。
 * 現在残高・見込み残高はDBに保存された値ではなく、都度算出した値を保持する（要件7.2, 9.2）。
 */
@Getter
@AllArgsConstructor
public class AssetBalanceView {

	private Long assetId;
	private String assetName;
	private AssetType assetType;
	private boolean active;

	/** 現在残高 = 初期残高 + 確定収入 - 確定支出 + 振替入金 - 振替出金 */
	private BigDecimal currentBalance;

	/** 見込み残高 = 現在残高 + 未確定の予定収入 - 未確定の予定支出 */
	private BigDecimal expectedBalance;
}