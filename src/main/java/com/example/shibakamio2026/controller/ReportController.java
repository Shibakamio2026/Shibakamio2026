package com.example.shibakamio2026.controller;

import java.time.YearMonth;
import java.time.format.DateTimeParseException;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.shibakamio2026.service.ReportService;
import com.example.shibakamio2026.web.CurrentUserService;

import lombok.RequiredArgsConstructor;

/**
 * G06 分析・レポート / G14 月々のグラフ表示 のコントローラ。
 */
@Controller
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

	private final ReportService reportService;
	private final CurrentUserService currentUserService;

	// ------------------------------------------------------------
	// G06 分析・レポート
	// ------------------------------------------------------------

	@GetMapping
	public String index(@RequestParam(required = false) String from,
			@RequestParam(required = false) String to,
			HttpSession session,
			Model model) {
		Long userId = currentUserService.getCurrentUserId(session);

		// デフォルトは「過去6か月分」（要件7.1, ユースケース1d）
		YearMonth defaultTo = YearMonth.now();
		YearMonth defaultFrom = defaultTo.minusMonths(5);

		YearMonth fromMonth = defaultFrom;
		YearMonth toMonth = defaultTo;
		boolean hasError = false;

		boolean paramsGiven = (from != null && !from.isBlank()) || (to != null && !to.isBlank());
		if (paramsGiven) {
			try {
				YearMonth parsedFrom = YearMonth.parse(from);
				YearMonth parsedTo = YearMonth.parse(to);
				if (parsedFrom.isAfter(parsedTo)) {
					hasError = true;
				} else {
					fromMonth = parsedFrom;
					toMonth = parsedTo;
				}
			} catch (DateTimeParseException | NullPointerException e) {
				hasError = true;
			}
		}

		if (hasError) {
			model.addAttribute("errorMessage", "正しい期間を選択してください");
		}

		var monthlySummaries = reportService.getMonthlySummaries(userId, fromMonth, toMonth);
		var categorySummaries = reportService.getCategorySummaries(userId, fromMonth, toMonth);

		model.addAttribute("fromMonth", fromMonth.toString());
		model.addAttribute("toMonth", toMonth.toString());
		model.addAttribute("monthlySummaries", monthlySummaries);
		model.addAttribute("categorySummaries", categorySummaries);
		// 要件14.3：表示対象データが存在しない場合は入力エラーではなく「データがありません」を表示する
		model.addAttribute("isEmpty", categorySummaries.isEmpty()
				&& monthlySummaries.stream()
						.allMatch(s -> s.getConfirmedIncome().signum() == 0 && s.getConfirmedExpense().signum() == 0
								&& s.getScheduledIncome().signum() == 0 && s.getScheduledExpense().signum() == 0));

		return "reports/index";
	}

	// ------------------------------------------------------------
	// G14 月々のグラフ表示
	// ------------------------------------------------------------

	@GetMapping("/monthly")
	public String monthly(@RequestParam(required = false) String month,
			HttpSession session,
			Model model) {
		Long userId = currentUserService.getCurrentUserId(session);

		YearMonth targetMonth = YearMonth.now();
		boolean hasError = false;

		if (month != null && !month.isBlank()) {
			try {
				targetMonth = YearMonth.parse(month);
			} catch (DateTimeParseException e) {
				hasError = true;
			}
		}

		if (hasError) {
			model.addAttribute("errorMessage", "未入力もしくは正しくない日付です");
		}

		var detail = reportService.getMonthlyDetail(userId, targetMonth);

		model.addAttribute("targetMonth", targetMonth.toString());
		model.addAttribute("prevMonth", targetMonth.minusMonths(1).toString());
		model.addAttribute("nextMonth", targetMonth.plusMonths(1).toString());
		model.addAttribute("detail", detail);

		boolean isEmpty = detail.getSummary().getConfirmedIncome().signum() == 0
				&& detail.getSummary().getConfirmedExpense().signum() == 0
				&& detail.getSummary().getScheduledIncome().signum() == 0
				&& detail.getSummary().getScheduledExpense().signum() == 0
				&& detail.getCategories().isEmpty();
		model.addAttribute("isEmpty", isEmpty);

		return "reports/monthly";
	}
}
