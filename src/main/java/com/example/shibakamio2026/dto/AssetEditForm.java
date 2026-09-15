package com.example.shibakamio2026.dto;

import com.example.shibakamio2026.enums.AssetType;

import lombok.Getter;
import lombok.Setter;

/**
 * G13 資産更新 の入力フォーム。
 * 初期残高は資産登録（G21）時のみ設定し、更新対象に含めない。
 */
@Getter
@Setter
public class AssetEditForm {

	private String assetName;

	private AssetType assetType;
}
