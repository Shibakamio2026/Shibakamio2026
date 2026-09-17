package com.example.shibakamio2026.controller;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.shibakamio2026.entity.Transaction;
import com.example.shibakamio2026.entity.User;
import com.example.shibakamio2026.repository.TransactionRepository;
import com.example.shibakamio2026.repository.UserRepository;

@Controller
@RequestMapping("/calendar")
public class CalendarController {

	/** 1マスに表示する取引の最大件数。これを超えた分は「他n件」にまとめる */
	private static final int MAX_ITEMS_PER_DAY = 2;

	private final UserRepository userRepository;
	private final TransactionRepository transactionRepository;

	public CalendarController(UserRepository userRepository, TransactionRepository transactionRepository) {
		this.userRepository = userRepository;
		this.transactionRepository = transactionRepository;
	}

	private User currentUser(Authentication authentication) {
		return userRepository.findByEmailIgnoreCase(authentication.getName())
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

		// category を fetch join 済み・日付→ID順で取得
		List<Transaction> transactions = transactionRepository.findForCalendar(user, start, end);

		Map<LocalDate, List<Transaction>> grouped = transactions.stream()
				.collect(Collectors.groupingBy(
						Transaction::getTransactionDate,
						LinkedHashMap::new,
						Collectors.toList()));

		// 表示用（先頭 MAX_ITEMS_PER_DAY 件）と、あふれた件数に分ける
		Map<LocalDate, List<Transaction>> byDate = new HashMap<>();
		Map<LocalDate, Integer> moreCount = new HashMap<>();

		for (Map.Entry<LocalDate, List<Transaction>> entry : grouped.entrySet()) {
			List<Transaction> all = entry.getValue();
			if (all.size() <= MAX_ITEMS_PER_DAY) {
				byDate.put(entry.getKey(), all);
			} else {
				byDate.put(entry.getKey(), new ArrayList<>(all.subList(0, MAX_ITEMS_PER_DAY)));
				moreCount.put(entry.getKey(), all.size() - MAX_ITEMS_PER_DAY);
			}
		}

		model.addAttribute("targetMonth", targetMonth);
		model.addAttribute("weeks", buildWeeks(targetMonth));
		model.addAttribute("byDate", byDate);
		model.addAttribute("moreCount", moreCount);
		model.addAttribute("today", LocalDate.now());
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