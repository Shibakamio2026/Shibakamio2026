package com.example.shibakamio2026.entity;

import java.math.BigDecimal;
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

import com.example.shibakamio2026.enums.AssetType;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 資産（口座・現金・電子マネー等）エンティティ。
 * G05 口座・資産一覧 / G13 資産更新 / G21 資産登録 で使用する。
 *
 * 現在残高・見込み残高はDBに保存せず、AssetService で都度算出する（要件7.2, 9.2）。
 */
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

	@Enumerated(EnumType.STRING)
	@Column(name = "asset_type", nullable = false, length = 50)
	private AssetType assetType;

	/** 登録時点の残高。資産登録（G21）でのみ設定し、以後は変更しない。 */
	@Column(name = "initial_balance", nullable = false, precision = 10, scale = 0)
	private BigDecimal initialBalance;

	@Column(name = "is_active", nullable = false)
	private Boolean isActive = true;

	@Column(name = "created_at", nullable = false)
	private OffsetDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private OffsetDateTime updatedAt;

	@PrePersist
	protected void onCreate() {
		OffsetDateTime now = OffsetDateTime.now();
		this.createdAt = now;
		this.updatedAt = now;
		if (this.isActive == null) {
			this.isActive = true;
		}
	}

	@PreUpdate
	protected void onUpdate() {
		this.updatedAt = OffsetDateTime.now();
	}
}
