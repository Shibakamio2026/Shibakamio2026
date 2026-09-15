package com.example.shibakamio2026.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

/**
 * G15 資産間振替 の入力フォーム。
 */
@Getter
@Setter
public class TransferForm {

	private Long fromAssetId;

	private Long toAssetId;

	private BigDecimal amount;

	private LocalDate transferDate;

	private String memo;
}
