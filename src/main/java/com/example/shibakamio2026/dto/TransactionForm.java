package com.example.shibakamio2026.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.example.shibakamio2026.enums.RecurrenceInterval;
import com.example.shibakamio2026.enums.TransactionType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransactionForm {

	private TransactionType transactionType;
	private LocalDate transactionDate;
	private BigDecimal amount;
	private Long categoryId;
	private Long assetId;
	private String memo;

	// "ONCE"（一度だけ登録）または "AUTO"（自動収支として登録）
	private String registrationType;

	private RecurrenceInterval intervalType;
	private LocalDate startDate;
	private LocalDate endDate;
}