package com.example.shibakamio2026.service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.shibakamio2026.dto.AssetBalanceView;
import com.example.shibakamio2026.dto.AssetCreateForm;
import com.example.shibakamio2026.dto.AssetEditForm;
import com.example.shibakamio2026.entity.Asset;
import com.example.shibakamio2026.entity.User;
import com.example.shibakamio2026.exception.ResourceNotFoundException;
import com.example.shibakamio2026.repository.AssetRepository;
import com.example.shibakamio2026.repository.TransferRepository;
import com.example.shibakamio2026.repository.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * 資産（口座・資産）管理のサービスクラス。
 * G05 口座・資産一覧 / G13 資産更新 / G21 資産登録 に対応する。
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AssetService {

	private final AssetRepository assetRepository;
	private final UserRepository userRepository;
	private final TransferRepository transferRepository;

	// ------------------------------------------------------------
	// G05 口座・資産一覧
	// ------------------------------------------------------------

	/** ログイン中ユーザーの資産を、残高情報付きで一覧取得する（無効化された資産も含む）。 */
	public List<AssetBalanceView> getAssetBalances(Long userId) {
		List<Asset> assets = assetRepository.findByUser_UserIdOrderByAssetIdAsc(userId);
		return assets.stream()
				.map(this::toBalanceView)
				.toList();
	}

	private AssetBalanceView toBalanceView(Asset asset) {
		BigDecimal currentBalance = calculateCurrentBalance(asset);
		BigDecimal expectedBalance = calculateExpectedBalance(asset, currentBalance);
		return new AssetBalanceView(
				asset.getAssetId(),
				asset.getAssetName(),
				asset.getAssetType(),
				Boolean.TRUE.equals(asset.getIsActive()),
				currentBalance,
				expectedBalance);
	}

	/**
	 * 現在残高 = 初期残高 + 確定収入 - 確定支出 + 振替入金 - 振替出金
	 * （transactions / scheduled_transactions はネイティブクエリで参照。要件9.2）
	 */
	public BigDecimal calculateCurrentBalance(Asset asset) {
		Long assetId = asset.getAssetId();
		BigDecimal income = assetRepository.sumConfirmedIncome(assetId);
		BigDecimal expense = assetRepository.sumConfirmedExpense(assetId);
		BigDecimal transferIn = transferRepository.sumTransferIn(assetId);
		BigDecimal transferOut = transferRepository.sumTransferOut(assetId);
		return asset.getInitialBalance()
				.add(income)
				.subtract(expense)
				.add(transferIn)
				.subtract(transferOut);
	}

	/** 見込み残高 = 現在残高 + 未確定の予定収入 - 未確定の予定支出（要件9.2） */
	public BigDecimal calculateExpectedBalance(Asset asset, BigDecimal currentBalance) {
		Long assetId = asset.getAssetId();
		BigDecimal scheduledIncome = assetRepository.sumScheduledIncome(assetId);
		BigDecimal scheduledExpense = assetRepository.sumScheduledExpense(assetId);
		return currentBalance.add(scheduledIncome).subtract(scheduledExpense);
	}

	// ------------------------------------------------------------
	// G21 資産登録
	// ------------------------------------------------------------

	/**
	 * 入力内容を検証する。エラーがあれば "フィールド名 -> エラーメッセージ" を返す（空なら正常）。
	 * メッセージ文言は入力チェック一覧の記載に合わせている。
	 */
	public Map<String, String> validateCreateForm(AssetCreateForm form) {
		Map<String, String> errors = new LinkedHashMap<>();
		if (isBlank(form.getAssetName())) {
			errors.put("assetName", "入力してください");
		}
		if (form.getAssetType() == null) {
			errors.put("assetType", "入力してください");
		}
		if (form.getInitialBalance() == null || form.getInitialBalance().compareTo(BigDecimal.ZERO) < 0) {
			errors.put("initialBalance", "未入力、または0未満の数字が入力されています。");
		}
		return errors;
	}

	@Transactional
	public Asset createAsset(Long userId, AssetCreateForm form) {
		User user = userRepository.getReferenceById(userId);

		Asset asset = new Asset();
		asset.setUser(user);
		asset.setAssetName(form.getAssetName());
		asset.setAssetType(form.getAssetType());
		asset.setInitialBalance(form.getInitialBalance());
		asset.setIsActive(true);
		return assetRepository.save(asset);
	}

	// ------------------------------------------------------------
	// G13 資産更新
	// ------------------------------------------------------------

	/** 編集対象の資産を取得する。存在しない、または他ユーザーの資産の場合は例外を投げる。 */
	public Asset getOwnedAsset(Long userId, Long assetId) {
		return assetRepository.findByAssetIdAndUser_UserId(assetId, userId)
				.orElseThrow(() -> new ResourceNotFoundException("資産が見つかりません"));
	}

	public Map<String, String> validateEditForm(AssetEditForm form) {
		Map<String, String> errors = new LinkedHashMap<>();
		if (isBlank(form.getAssetName())) {
			errors.put("assetName", "入力してください");
		}
		if (form.getAssetType() == null) {
			errors.put("assetType", "入力してください");
		}
		return errors;
	}

	@Transactional
	public void updateAsset(Long userId, Long assetId, AssetEditForm form) {
		Asset asset = getOwnedAsset(userId, assetId);
		asset.setAssetName(form.getAssetName());
		asset.setAssetType(form.getAssetType());
		// 初期残高は更新対象に含めない（G13の仕様）
		assetRepository.save(asset);
	}

	/** 使用中の資産は削除せず無効化する（要件9.5）。無効化後は新規収支・振替の登録先として選択できない。 */
	@Transactional
	public void deactivateAsset(Long userId, Long assetId) {
		Asset asset = getOwnedAsset(userId, assetId);
		asset.setIsActive(false);
		assetRepository.save(asset);
	}

	private boolean isBlank(String s) {
		return s == null || s.isBlank();
	}
}
