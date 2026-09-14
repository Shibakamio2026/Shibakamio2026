package com.example.shibakamio2026.entity;

import java.math.BigDecimal;
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
@Table(name = "assets")
@Getter
@Setter
@NoArgsConstructor
public class Asset {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "asset_id")
	private Long assetId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(name = "asset_name", nullable = false, length = 50)
	private String assetName;

	@Column(name = "asset_type", nullable = false, length = 50)
	private String assetType;

	@Column(name = "initial_balance", nullable = false, precision = 10, scale = 0)
	private BigDecimal initialBalance;

	@Column(name = "is_active", nullable = false)
	private boolean active = true;

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