package com.example.shibakamio2026.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import com.example.shibakamio2026.enums.ScheduledStatus;
import com.example.shibakamio2026.enums.TransactionType;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "scheduled_transactions")
@Getter
@Setter
@NoArgsConstructor
public class ScheduledTransaction {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "scheduled_transaction_id")
	private Long scheduledTransactionId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "auto_transaction_id", nullable = false)
	private AutoTransaction autoTransaction;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "asset_id", nullable = false)
	private Asset asset;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "category_id", nullable = false)
	private Category category;

	@Enumerated(EnumType.STRING)
	@Column(name = "transaction_type", nullable = false, length = 10)
	private TransactionType transactionType;

	@Column(name = "scheduled_date", nullable = false)
	private LocalDate scheduledDate;

	@Column(name = "amount", nullable = false, precision = 10, scale = 0)
	private BigDecimal amount;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 20)
	private ScheduledStatus status;

	@Column(name = "memo")
	private String memo;

	@Column(name = "created_at", nullable = false)
	private OffsetDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private OffsetDateTime updatedAt;

	@PrePersist
	protected void onCreate() {
		OffsetDateTime now = OffsetDateTime.now();
		this.createdAt = now;
		this.updatedAt = now;
	}

	@PreUpdate
	protected void onUpdate() {
		this.updatedAt = OffsetDateTime.now();
	}
}