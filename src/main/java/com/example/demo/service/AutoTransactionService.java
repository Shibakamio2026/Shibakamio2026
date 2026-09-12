package com.example.demo.service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.entity.AutoTransaction;
import com.example.demo.entity.ScheduledTransaction;
import com.example.demo.enums.RecurrenceInterval;
import com.example.demo.enums.ScheduledStatus;
import com.example.demo.repository.AutoTransactionRepository;
import com.example.demo.repository.ScheduledTransactionRepository;

@Service
public class AutoTransactionService {

	private static final int GENERATE_MONTHS_AHEAD = 12;

	private final AutoTransactionRepository autoTransactionRepository;
	private final ScheduledTransactionRepository scheduledTransactionRepository;

	public AutoTransactionService(AutoTransactionRepository autoTransactionRepository,
			ScheduledTransactionRepository scheduledTransactionRepository) {
		this.autoTransactionRepository = autoTransactionRepository;
		this.scheduledTransactionRepository = scheduledTransactionRepository;
	}

	public AutoTransaction createAutoTransaction(AutoTransaction autoTransaction) {
		AutoTransaction saved = autoTransactionRepository.save(autoTransaction);
		generateScheduledTransactions(saved);
		return saved;
	}

	public void generateScheduledTransactions(AutoTransaction auto) {
		int originalDay = auto.getStartDate().getDayOfMonth();
		LocalDate limit = LocalDate.now().plusMonths(GENERATE_MONTHS_AHEAD);
		LocalDate end = (auto.getEndDate() != null && auto.getEndDate().isBefore(limit))
				? auto.getEndDate()
				: limit;

		List<LocalDate> dates = new ArrayList<>();
		LocalDate current = auto.getStartDate();

		while (!current.isAfter(end)) {
			dates.add(current);
			current = nextDate(current, auto.getIntervalType(), originalDay);
		}

		for (LocalDate date : dates) {
			ScheduledTransaction st = new ScheduledTransaction();
			st.setAutoTransaction(auto);
			st.setAsset(auto.getAsset());
			st.setCategory(auto.getCategory());
			st.setTransactionType(auto.getTransactionType());
			st.setScheduledDate(date);
			st.setAmount(auto.getAmount());
			st.setStatus(ScheduledStatus.PLANNED);
			scheduledTransactionRepository.save(st);
		}
	}

	private LocalDate nextDate(LocalDate current, RecurrenceInterval interval, int originalDay) {
		switch (interval) {
		case WEEKLY:
			return current.plusWeeks(1);
		case MONTHLY: {
			YearMonth nextMonth = YearMonth.from(current).plusMonths(1);
			int day = Math.min(originalDay, nextMonth.lengthOfMonth());
			return nextMonth.atDay(day);
		}
		case YEARLY: {
			YearMonth nextYear = YearMonth.from(current).plusYears(1);
			int day = Math.min(originalDay, nextYear.lengthOfMonth());
			return nextYear.atDay(day);
		}
		default:
			throw new IllegalArgumentException("未対応の発生間隔です");
		}
	}
}