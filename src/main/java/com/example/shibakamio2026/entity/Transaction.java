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
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import com.example.shibakamio2026.enums.TransactionType;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
public class Transaction {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "transaction_id")
	private Long transactionId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "asset_id", nullable = false)
	private Asset asset;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "category_id", nullable = false)
	private Category category;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "scheduled_transaction_id", unique = true)
	private ScheduledTransaction scheduledTransaction;

	@Enumerated(EnumType.STRING)
	@Column(name = "transaction_type", nullable = false, length = 10)
	private TransactionType transactionType;

	@Column(name = "transaction_date", nullable = false)
	private LocalDate transactionDate;

	@Column(name = "amount", nullable = false, precision = 10, scale = 0)
	private BigDecimal amount;

	/** CSV「取引内容」（例：支払い／チャージ／送金受け取り） */
	@Column(name = "transaction_content")
	private String transactionContent;

	/** CSV「取引先」 */
	@Column(name = "business_partner")
	private String businessPartner;

	/** CSV「取引方法」（例：PayPay残高／PayPayカード） */
	@Column(name = "transaction_method")
	private String transactionMethod;

	/** CSV「支払い区分」（例：ー回払い） */
	@Column(name = "payment_type")
	private String paymentType;

	/** CSV「利用者」 */
	@Column(name = "user_name")
	private String userName;

	/** CSV「取引番号」（重複判定に使用） */
	@Column(name = "transaction_number")
	private String transactionNumber;

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