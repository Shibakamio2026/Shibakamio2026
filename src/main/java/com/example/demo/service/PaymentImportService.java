package com.example.demo.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.CsvImportRow;
import com.example.demo.entity.Asset;
import com.example.demo.entity.Category;
import com.example.demo.entity.Transaction;
import com.example.demo.entity.Transfer;
import com.example.demo.entity.User;
import com.example.demo.enums.TransactionType;
import com.example.demo.repository.AssetRepository;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.TransactionRepository;
import com.example.demo.repository.TransferRepository;

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

	public List<CsvImportRow> buildPreview(MultipartFile file, User user) throws IOException {
		List<CsvImportRow> rows = payPayCsvParser.parse(file.getInputStream());
		List<Transaction> existing = transactionRepository.findByAsset_UserOrderByTransactionDateDesc(user);

		for (CsvImportRow row : rows) {
			BigDecimal amount = "EXPENSE".equals(row.getCandidateType())
					? row.getWithdrawalAmount()
					: row.getDepositAmount();

			boolean duplicate = existing.stream().anyMatch(t -> t.getTransactionDate().equals(row.getTransactionDate())
					&& t.getAmount().compareTo(amount) == 0);
			row.setDuplicateWarning(duplicate);
		}

		return rows;
	}

	public void importRows(List<CsvImportRow> rows, User user, Asset targetAsset) {
		for (CsvImportRow row : rows) {
			if (!row.isChecked()) {
				continue;
			}

			switch (row.getCandidateType()) {
			case "EXPENSE" -> saveTransaction(row, targetAsset, TransactionType.EXPENSE, row.getWithdrawalAmount());
			case "OTHER_INCOME" -> saveTransaction(row, targetAsset, TransactionType.INCOME, row.getDepositAmount());
			case "CHARGE" -> saveTransfer(row, targetAsset);
			default -> throw new IllegalStateException("未対応の種別です: " + row.getCandidateType());
			}
		}
	}

	private void saveTransaction(CsvImportRow row, Asset targetAsset, TransactionType type, BigDecimal amount) {
		Category category = categoryRepository.findById(row.getCategoryId())
				.orElseThrow(() -> new IllegalArgumentException("カテゴリーを選択してください"));

		Transaction t = new Transaction();
		t.setAsset(targetAsset);
		t.setCategory(category);
		t.setTransactionType(type);
		t.setTransactionDate(row.getTransactionDate());
		t.setAmount(amount);
		t.setSourceContent(row.getContent() + " / " + row.getBusinessPartner());
		t.setExternalTransactionNo(row.getTransactionNumber());
		transactionRepository.save(t);
	}

	private void saveTransfer(CsvImportRow row, Asset targetAsset) {
		Asset sourceAsset = assetRepository.findById(row.getSourceAssetId())
				.orElseThrow(() -> new IllegalArgumentException("振替元資産を選択してください"));

		Transfer transfer = new Transfer();
		transfer.setFromAsset(sourceAsset);
		transfer.setToAsset(targetAsset);
		transfer.setAmount(row.getDepositAmount());
		transfer.setTransferDate(row.getTransactionDate());
		transfer.setSourceContent(row.getContent());
		transfer.setExternalTransactionNo(row.getTransactionNumber());
		transferRepository.save(transfer);
	}
}