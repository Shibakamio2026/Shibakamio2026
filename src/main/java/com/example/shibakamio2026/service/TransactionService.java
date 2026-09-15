package com.example.shibakamio2026.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.shibakamio2026.entity.Transaction;
import com.example.shibakamio2026.entity.User;
import com.example.shibakamio2026.repository.TransactionRepository;

@Service
public class TransactionService {

	private final TransactionRepository transactionRepository;

	public TransactionService(TransactionRepository transactionRepository) {
		this.transactionRepository = transactionRepository;
	}

	public Transaction registerOneTime(Transaction transaction) {
		return transactionRepository.save(transaction);
	}

	// ===== G18 収支一覧 =====

	public List<Transaction> findAllForUser(User user) {
		return transactionRepository.findByAsset_UserOrderByTransactionDateDesc(user);
	}

	// ===== G19 収支詳細・編集 =====

	public Transaction findByIdForUser(Long transactionId, User user) {
		Transaction transaction = transactionRepository.findById(transactionId)
				.orElseThrow(() -> new IllegalArgumentException("収支が見つかりません"));

		if (!transaction.getAsset().getUser().getUserId().equals(user.getUserId())) {
			throw new IllegalStateException("この収支にアクセスする権限がありません");
		}
		return transaction;
	}

	public Transaction update(Transaction transaction) {
		return transactionRepository.save(transaction);
	}

	public void delete(Transaction transaction) {
		transactionRepository.delete(transaction);
	}
}