package com.example.shibakamio2026.enums;

/**
 * 資産種別。
 * assets.asset_type カラム（VARCHAR(50)）に文字列（enum名）として保存する。
 */
public enum AssetType {
	CASH("現金"), BANK("銀行口座"), E_MONEY("電子マネー"), OTHER("その他");

	private final String label;

	AssetType(String label) {
		this.label = label;
	}

	/** 画面表示用の日本語ラベル */
	public String getLabel() {
		return label;
	}
}
