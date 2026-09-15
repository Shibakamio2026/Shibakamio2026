package com.example.shibakamio2026.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.shibakamio2026.dto.CsvImportRow;
import com.example.shibakamio2026.entity.Asset;
import com.example.shibakamio2026.entity.Category;
import com.example.shibakamio2026.entity.Transaction;
import com.example.shibakamio2026.entity.Transfer;
import com.example.shibakamio2026.entity.User;
import com.example.shibakamio2026.enums.TransactionType;
import com.example.shibakamio2026.repository.AssetRepository;
import com.example.shibakamio2026.repository.CategoryRepository;
import com.example.shibakamio2026.repository.TransactionRepository;
import com.example.shibakamio2026.repository.TransferRepository;

@Service
public class PaymentImportService {

	private final PayPayCsvParser payPayCsvParser;
	private final TransactionRepository transactionRepository;
	private final TransferRepository transferRepository;
	private final CategoryRepository categoryRepository;
	private final AssetRepository assetRepository;

	public PaymentImportService(PayPayCsvParser payPayCsvParser,
			TransactionRepository transactionRepository,
			TransferRepository transferRepository,
			CategoryRepository categoryRepository,
			AssetRepository assetRepository) {
		this.payPayCsvParser = payPayCsvParser;
		this.transactionRepository = transactionRepository;
		this.transferRepository = transferRepository;
		this.categoryRepository = categoryRepository;
		this.assetRepository = assetRepository;
	}

	/**
	 * @param defaultAssetId 取込画面で選んだ資産（チャージの振替先、および各行の登録先資産の初期値）
	 */
	public List<CsvImportRow> buildPreview(MultipartFile file, User user, Long defaultAssetId) throws IOException {
		List<CsvImportRow> rows = payPayCsvParser.parse(file.getInputStream());
		List<Transaction> existing = transactionRepository.findByAsset_UserOrderByTransactionDateDesc(user);

		for (CsvImportRow row : rows) {
			BigDecimal amount = "EXPENSE".equals(row.getCandidateType())
					? row.getWithdrawalAmount()
					: row.getDepositAmount();

			boolean duplicate = existing.stream().anyMatch(t -> t.getTransactionDate().equals(row.getTransactionDate())
					&& t.getAmount().compareTo(amount) == 0);
			row.setDuplicateWarning(duplicate);

			// 支出・その他入金候補は、取込画面で選んだ資産を初期値として選択済みにしておく
			if (!"CHARGE".equals(row.getCandidateType())) {
				row.setTargetAssetId(defaultAssetId);
			}
		}

		return rows;
	}

	public void importRows(List<CsvImportRow> rows, User user, Asset chargeToAsset) {
		for (CsvImportRow row : rows) {
			if (!row.isChecked()) {
				continue;
			}

			switch (row.getCandidateType()) {
			case "EXPENSE" -> saveTransaction(row, TransactionType.EXPENSE, row.getWithdrawalAmount());
			case "OTHER_INCOME" -> saveTransaction(row, TransactionType.INCOME, row.getDepositAmount());
			case "CHARGE" -> saveTransfer(row, chargeToAsset);
			default -> throw new IllegalStateException("未対応の種別です: " + row.getCandidateType());
			}
		}
	}

	private void saveTransaction(CsvImportRow row, TransactionType type, BigDecimal amount) {
		Category category = categoryRepository.findById(row.getCategoryId())
				.orElseThrow(() -> new IllegalArgumentException("カテゴリーを選択してください"));
		Asset targetAsset = assetRepository.findById(row.getTargetAssetId())
				.orElseThrow(() -> new IllegalArgumentException("登録先の資産を選択してください"));

		Transaction t = new Transaction();
		t.setAsset(targetAsset);
		t.setCategory(category);
		t.setTransactionType(type);
		t.setTransactionDate(row.getTransactionDate());
		t.setAmount(amount);
		t.setTransactionContent(row.getContent());
		t.setBusinessPartner(row.getBusinessPartner());
		t.setTransactionMethod(row.getTransactionMethod());
		t.setPaymentType(row.getPaymentType());
		t.setUserName(row.getUserName());
		t.setTransactionNumber(row.getTransactionNumber());
		transactionRepository.save(t);
	}

	private void saveTransfer(CsvImportRow row, Asset chargeToAsset) {
		Asset sourceAsset = assetRepository.findById(row.getSourceAssetId())
				.orElseThrow(() -> new IllegalArgumentException("振替元資産を選択してください"));

		Transfer transfer = new Transfer();
		transfer.setFromAsset(sourceAsset);
		transfer.setToAsset(chargeToAsset);
		transfer.setAmount(row.getDepositAmount());
		transfer.setTransferDate(row.getTransactionDate());
		transfer.setSourceContent(row.getContent());
		transfer.setExternalTransactionNo(row.getTransactionNumber());
		transferRepository.save(transfer);
	}
}