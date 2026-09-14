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

	@Column(name = "external_transaction_no")
	private String externalTransactionNo;

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