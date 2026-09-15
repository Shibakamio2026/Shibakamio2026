package com.example.shibakamio2026.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.example.shibakamio2026.enums.TransactionType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransactionEditForm {

	private TransactionType transactionType;
	private LocalDate transactionDate;
	private BigDecimal amount;
	private Long categoryId;
	private Long assetId;
	private String memo;
}