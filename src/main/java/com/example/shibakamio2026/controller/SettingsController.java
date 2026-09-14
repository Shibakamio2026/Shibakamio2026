package com.example.shibakamio2026.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * G09 設定 のコントローラ。各種設定画面への入口。
 */
@Controller
@RequestMapping("/settings")
public class SettingsController {

	@GetMapping
	public String index() {
		return "settings/index";
	}
}
