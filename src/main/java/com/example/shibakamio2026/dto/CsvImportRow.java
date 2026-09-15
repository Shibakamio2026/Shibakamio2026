package com.example.shibakamio2026.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CsvImportRow implements java.io.Serializable {
	private static final long serialVersionUID = 1L;

	private LocalDate transactionDate;
	private BigDecimal withdrawalAmount;
	private BigDecimal depositAmount;
	private String content;
	private String businessPartner;
	private String transactionNumber;

	/** EXPENSE（支出）, CHARGE（チャージ）, OTHER_INCOME（その他入金） */
	private String candidateType;

	private Long categoryId;
	private Long sourceAssetId;

	private boolean duplicateWarning;
	private boolean checked = true;
}