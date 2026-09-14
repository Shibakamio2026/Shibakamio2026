package com.example.shibakamio2026.dto;

import java.math.BigDecimal;

import com.example.shibakamio2026.enums.AssetType;

import lombok.Getter;
import lombok.Setter;

/**
 * G21 資産登録 の入力フォーム。
 * 資産名・資産種別・初期残高（doc仕様どおり必須3項目）。
 */
@Getter
@Setter
public class AssetCreateForm {

	private String assetName;

	private AssetType assetType;

	private BigDecimal initialBalance;
}
