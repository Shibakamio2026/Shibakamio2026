package com.example.demo.service;

import org.springframework.stereotype.Service;

import com.example.demo.entity.Transaction;
import com.example.demo.repository.TransactionRepository;

@Service
public class TransactionService {

	private final TransactionRepository transactionRepository;

	public TransactionService(TransactionRepository transactionRepository) {
		this.transactionRepository = transactionRepository;
	}

	public Transaction registerOneTime(Transaction transaction) {
		return transactionRepository.save(transaction);
	}
}