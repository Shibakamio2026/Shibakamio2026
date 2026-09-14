package com.example.shibakamio2026.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.shibakamio2026.entity.Transfer;

public interface TransferRepository extends JpaRepository<Transfer, Long> {
}