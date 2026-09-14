package com.example.shibakamio2026.controller;

import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.Map;

import jakarta.servlet.http.HttpSession;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.shibakamio2026.dto.MonthlyChartResponse;
import com.example.shibakamio2026.service.ReportService;
import com.example.shibakamio2026.web.CurrentUserService;

import lombok.RequiredArgsConstructor;

/**
 * グラフ表示用データのJSON出力（要件8章）。
 * ログイン中の利用者自身のデータのみを出力する（8.3）。userIdはセッションから取得し、
 * リクエストパラメータで他ユーザーのデータを指定することはできない。
 */
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportApiController {

	private final ReportService reportService;
	private final CurrentUserService currentUserService;

	/**
	 * 例：GET /api/reports/monthly-chart?from=2026-04&amp;to=2026-09
	 * from/to を省略した場合は、直近6か月分を対象とする。
	 */
	@GetMapping("/monthly-chart")
	public ResponseEntity<?> monthlyChart(@RequestParam(required = false) String from,
			@RequestParam(required = false) String to,
			HttpSession session) {
		Long userId = currentUserService.getCurrentUserId(session);

		YearMonth toMonth = YearMonth.now();
		YearMonth fromMonth = toMonth.minusMonths(5);

		try {
			if (to != null && !to.isBlank()) {
				toMonth = YearMonth.parse(to);
			}
			if (from != null && !from.isBlank()) {
				fromMonth = YearMonth.parse(from);
			}
		} catch (DateTimeParseException e) {
			return ResponseEntity.badRequest().body(Map.of("error", "正しい期間を選択してください"));
		}

		if (fromMonth.isAfter(toMonth)) {
			return ResponseEntity.badRequest().body(Map.of("error", "正しい期間を選択してください"));
		}

		MonthlyChartResponse response = reportService.buildJsonExport(userId, fromMonth, toMonth);
		return ResponseEntity.ok(response);
	}
}
