package com.example.shibakamio2026.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.example.shibakamio2026.dto.CsvImportRow;
import com.example.shibakamio2026.exception.InvalidCsvFormatException;

/**
 * PayPay明細CSVのパーサー。
 * 列構成（要件定義書どおり）：
 * 0:取引日 1:出金金額 2:入金金額 3:取引内容 4:取引先 5:取引方法 6:支払い区分 7:利用者 8:取引番号
 */
@Component
public class PayPayCsvParser {

	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd");
	private static final int MIN_COLUMNS = 9;

	public List<CsvImportRow> parse(InputStream inputStream) throws IOException {
		List<CsvImportRow> rows = new ArrayList<>();

		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

			String headerLine = reader.readLine();
			if (headerLine == null) {
				throw new InvalidCsvFormatException("CSVファイルが空です");
			}

			String line;
			int lineNumber = 1;
			while ((line = reader.readLine()) != null) {
				lineNumber++;
				if (line.isBlank()) {
					continue;
				}

				String[] cols = line.split(",", -1);
				if (cols.length < MIN_COLUMNS) {
					throw new InvalidCsvFormatException(lineNumber + "行目: 列数が不足しています");
				}

				CsvImportRow row = new CsvImportRow();
				try {
					row.setTransactionDate(LocalDate.parse(cols[0].trim(), DATE_FORMAT));
				} catch (Exception e) {
					throw new InvalidCsvFormatException(lineNumber + "行目: 取引日の形式が不正です");
				}
				row.setWithdrawalAmount(parseAmount(cols[1]));
				row.setDepositAmount(parseAmount(cols[2]));
				row.setContent(cols[3].trim());
				row.setBusinessPartner(cols[4].trim());
				row.setTransactionMethod(cols[5].trim());
				row.setPaymentType(cols[6].trim());
				row.setUserName(cols[7].trim());
				row.setTransactionNumber(cols[8].trim());

				if (row.getContent().contains("チャージ")) {
					row.setCandidateType("CHARGE");
				} else if (row.getWithdrawalAmount().compareTo(BigDecimal.ZERO) > 0) {
					row.setCandidateType("EXPENSE");
				} else {
					row.setCandidateType("OTHER_INCOME");
				}

				rows.add(row);
			}
		}

		return rows;
	}

	private BigDecimal parseAmount(String raw) {
		String cleaned = raw.trim().replace(",", "");
		if (cleaned.isEmpty() || cleaned.equals("-")) {
			return BigDecimal.ZERO;
		}
		return new BigDecimal(cleaned);
	}
}