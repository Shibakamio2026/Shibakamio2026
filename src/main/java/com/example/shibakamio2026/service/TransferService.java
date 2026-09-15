package com.example.shibakamio2026.service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.shibakamio2026.dto.TransferForm;
import com.example.shibakamio2026.entity.Asset;
import com.example.shibakamio2026.entity.Transfer;
import com.example.shibakamio2026.exception.ResourceNotFoundException;
import com.example.shibakamio2026.repository.AssetRepository;
import com.example.shibakamio2026.repository.TransferRepository;

import lombok.RequiredArgsConstructor;

/**
 * 資産間振替のサービスクラス。G15 資産間振替 に対応する。
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransferService {

	private final TransferRepository transferRepository;
	private final AssetRepository assetRepository;

	/** 振替元・振替先の選択肢として使う、ログイン中ユーザーの有効な資産一覧。 */
	public List<Asset> getSelectableAssets(Long userId) {
		return assetRepository.findByUser_UserIdOrderByAssetIdAsc(userId).stream()
				.filter(a -> Boolean.TRUE.equals(a.getIsActive()))
				.toList();
	}

	/**
	 * 入力内容を検証する。エラーがあれば "フィールド名 -> エラーメッセージ" を返す（空なら正常）。
	 * 振替元・振替先が実際にログイン中ユーザーの資産であるかもここで確認する。
	 */
	public Map<String, String> validate(Long userId, TransferForm form) {
		Map<String, String> errors = new LinkedHashMap<>();

		boolean fromValid = form.getFromAssetId() != null
				&& assetRepository.findByAssetIdAndUser_UserId(form.getFromAssetId(), userId).isPresent();
		boolean toValid = form.getToAssetId() != null
				&& assetRepository.findByAssetIdAndUser_UserId(form.getToAssetId(), userId).isPresent();

		if (!fromValid) {
			errors.put("fromAssetId", "選択してください");
		}

		if (!toValid
				|| (form.getFromAssetId() != null && form.getFromAssetId().equals(form.getToAssetId()))) {
			errors.put("toAssetId", "未選択の項目もしくは同じ項目を選択しております");
		}

		if (form.getAmount() == null || form.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
			errors.put("amount", "未入力か０より多くない金額です");
		}

		if (form.getTransferDate() == null) {
			errors.put("transferDate", "未入力もしくは無効な日付が入力されております");
		}

		return errors;
	}

	@Transactional
	public Transfer createTransfer(Long userId, TransferForm form) {
		Asset fromAsset = assetRepository.findByAssetIdAndUser_UserId(form.getFromAssetId(), userId)
				.orElseThrow(() -> new ResourceNotFoundException("振替元資産が見つかりません"));
		Asset toAsset = assetRepository.findByAssetIdAndUser_UserId(form.getToAssetId(), userId)
				.orElseThrow(() -> new ResourceNotFoundException("振替先資産が見つかりません"));

		Transfer transfer = new Transfer();
		transfer.setFromAsset(fromAsset);
		transfer.setToAsset(toAsset);
		transfer.setAmount(form.getAmount());
		transfer.setTransferDate(form.getTransferDate());
		transfer.setMemo(form.getMemo());
		// 残高は保存せず、都度算出（AssetService）で反映されるため、ここでは登録するのみでよい。
		return transferRepository.save(transfer);
	}
}
