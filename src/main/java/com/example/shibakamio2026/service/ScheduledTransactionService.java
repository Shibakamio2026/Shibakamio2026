package com.example.shibakamio2026.service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import com.example.shibakamio2026.entity.ScheduledTransaction;
import com.example.shibakamio2026.entity.Transaction;
import com.example.shibakamio2026.entity.User;
import com.example.shibakamio2026.enums.ScheduledStatus;
import com.example.shibakamio2026.repository.ScheduledTransactionRepository;
import com.example.shibakamio2026.repository.TransactionRepository;

@Service
public class ScheduledTransactionService {

	private final ScheduledTransactionRepository scheduledTransactionRepository;
	private final TransactionRepository transactionRepository;

	public ScheduledTransactionService(ScheduledTransactionRepository scheduledTransactionRepository,
			TransactionRepository transactionRepository) {
		this.scheduledTransactionRepository = scheduledTransactionRepository;
		this.transactionRepository = transactionRepository;
	}

	// ===== G20 予定収支確認 =====

	public List<ScheduledTransaction> findPendingForUser(User user) {
		return Stream.concat(
				scheduledTransactionRepository.findByAsset_UserAndStatus(user, ScheduledStatus.PLANNED).stream(),
				scheduledTransactionRepository.findByAsset_UserAndStatus(user, ScheduledStatus.PENDING_CONFIRMATION)
						.stream())
				.sorted((a, b) -> a.getScheduledDate().compareTo(b.getScheduledDate()))
				.collect(Collectors.toList());
	}

	public ScheduledTransaction findByIdForUser(Long id, User user) {
		ScheduledTransaction st = scheduledTransactionRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("予定収支が見つかりません"));
		if (!st.getAsset().getUser().getUserId().equals(user.getUserId())) {
			throw new IllegalStateException("この予定収支にアクセスする権限がありません");
		}
		return st;
	}

	public Transaction confirm(Long id, User user) {
		ScheduledTransaction st = findByIdForUser(id, user);

		Transaction transaction = new Transaction();
		transaction.setAsset(st.getAsset());
		transaction.setCategory(st.getCategory());
		transaction.setScheduledTransaction(st);
		transaction.setTransactionType(st.getTransactionType());
		transaction.setTransactionDate(st.getScheduledDate());
		transaction.setAmount(st.getAmount());
		transaction.setMemo(st.getMemo());
		Transaction saved = transactionRepository.save(transaction);

		st.setStatus(ScheduledStatus.CONFIRMED);
		scheduledTransactionRepository.save(st);

		return saved;
	}

	public void cancel(Long id, User user) {
		ScheduledTransaction st = findByIdForUser(id, user);
		st.setStatus(ScheduledStatus.CANCELED);
		scheduledTransactionRepository.save(st);
	}
}