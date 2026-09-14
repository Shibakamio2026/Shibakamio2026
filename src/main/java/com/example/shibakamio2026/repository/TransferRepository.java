package com.example.shibakamio2026.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.shibakamio2026.entity.Transfer;

public interface TransferRepository extends JpaRepository<Transfer, Long> {

	List<Transfer> findByFromAsset_AssetIdOrToAsset_AssetIdOrderByTransferDateDesc(Long fromAssetId, Long toAssetId);

	@Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transfer t WHERE t.toAsset.assetId = :assetId")
	BigDecimal sumTransferIn(@Param("assetId") Long assetId);

	@Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transfer t WHERE t.fromAsset.assetId = :assetId")
	BigDecimal sumTransferOut(@Param("assetId") Long assetId);
}
