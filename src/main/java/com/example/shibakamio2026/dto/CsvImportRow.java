package com.example.shibakamio2026.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CsvImportRow implements java.io.Serializable {
	private static final long serialVersionUID = 2L;

	private LocalDate transactionDate;
	private BigDecimal withdrawalAmount;
	private BigDecimal depositAmount;
	private String content;
	private String businessPartner;
	private String transactionMethod;
	private String paymentType;
	private String userName;
	private String transactionNumber;

	/** EXPENSE（支出）, CHARGE（チャージ）, OTHER_INCOME（その他入金） */
	private String candidateType;

	private Long categoryId;

	/** 支出・その他入金候補の登録先資産（未選択時は取込画面で選んだ資産を初期値とする） */
	private Long targetAssetId;

	/** チャージ候補の振替元資産 */
	private Long sourceAssetId;

	private boolean duplicateWarning;
	private boolean checked = true;
}