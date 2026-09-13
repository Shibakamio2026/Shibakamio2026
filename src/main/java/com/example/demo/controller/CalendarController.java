package com.example.demo.controller;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.entity.Transaction;
import com.example.demo.entity.User;
import com.example.demo.enums.TransactionType;
import com.example.demo.repository.TransactionRepository;
import com.example.demo.repository.UserRepository;

@Controller
@RequestMapping("/calendar")
public class CalendarController {

	private final UserRepository userRepository;
	private final TransactionRepository transactionRepository;

	public CalendarController(UserRepository userRepository, TransactionRepository transactionRepository) {
		this.userRepository = userRepository;
		this.transactionRepository = transactionRepository;
	}

	private User currentUser(Authentication authentication) {
		return userRepository.findByEmail(authentication.getName())
				.orElseThrow(() -> new IllegalStateException("ユーザーが見つかりません"));
	}

	@GetMapping
	public String calendar(@RequestParam(required = false) Integer year,
			@RequestParam(required = false) Integer month,
			Authentication authentication,
			Model model) {

		User user = currentUser(authentication);
		YearMonth targetMonth = (year != null && month != null)
				? YearMonth.of(year, month)
				: YearMonth.now();

		LocalDate start = targetMonth.atDay(1);
		LocalDate end = targetMonth.atEndOfMonth();

		List<Transaction> transactions = transactionRepository
				.findByAsset_UserAndTransactionDateBetweenOrderByTransactionDateAsc(user, start, end);

		Map<LocalDate, List<Transaction>> byDate = transactions.stream()
				.collect(Collectors.groupingBy(Transaction::getTransactionDate));

		Map<LocalDate, BigDecimal> netTotal = new HashMap<>();
		for (Map.Entry<LocalDate, List<Transaction>> entry : byDate.entrySet()) {
			BigDecimal net = entry.getValue().stream()
					.map(t -> t.getTransactionType() == TransactionType.INCOME
							? t.getAmount()
							: t.getAmount().negate())
					.reduce(BigDecimal.ZERO, BigDecimal::add);
			netTotal.put(entry.getKey(), net);
		}

		model.addAttribute("targetMonth", targetMonth);
		model.addAttribute("weeks", buildWeeks(targetMonth));
		model.addAttribute("byDate", byDate);
		model.addAttribute("netTotal", netTotal);
		model.addAttribute("prevMonth", targetMonth.minusMonths(1));
		model.addAttribute("nextMonth", targetMonth.plusMonths(1));
		return "calendar";
	}

	private List<List<LocalDate>> buildWeeks(YearMonth month) {
		List<List<LocalDate>> weeks = new ArrayList<>();
		LocalDate first = month.atDay(1);
		int offset = first.getDayOfWeek() == DayOfWeek.SUNDAY ? 0 : first.getDayOfWeek().getValue();
		LocalDate cursor = first.minusDays(offset);

		LocalDate lastDayOfGrid = month.atEndOfMonth();
		while (true) {
			List<LocalDate> week = new ArrayList<>();
			for (int i = 0; i < 7; i++) {
				week.add(cursor);
				cursor = cursor.plusDays(1);
			}
			weeks.add(week);
			if (cursor.isAfter(lastDayOfGrid)) {
				break;
			}
		}
		return weeks;
	}
}