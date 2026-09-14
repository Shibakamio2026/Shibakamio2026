package com.example.shibakamio2026.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 資産間振替エンティティ。G15 資産間振替 で使用する。
 * 収入・支出（transactions）とは区別して管理する（要件9.3）。
 */
@Entity
@Table(name = "transfers")
@Getter
@Setter
@NoArgsConstructor
public class Transfer {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "transfer_id")
	private Long transferId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "from_asset_id", nullable = false)
	private Asset fromAsset;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "to_asset_id", nullable = false)
	private Asset toAsset;

	@Column(name = "amount", nullable = false, precision = 10, scale = 0)
	private BigDecimal amount;

	@Column(name = "transfer_date", nullable = false)
	private LocalDate transferDate;

	/** PayPay CSV取込（チャージ候補）から登録された場合の取引番号。手動登録時はnull。 */
	@Column(name = "external_transaction_no")
	private String externalTransactionNo;

	/** CSV取込元の取引内容。手動登録時はnull。 */
	@Column(name = "source_content")
	private String sourceContent;

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
