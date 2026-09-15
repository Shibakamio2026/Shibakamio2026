package com.example.shibakamio2026.config;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.example.shibakamio2026.entity.User;
import com.example.shibakamio2026.repository.UserRepository;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

	private final UserRepository userRepository;

	public CustomAuthenticationSuccessHandler(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {

		String email = authentication.getName();
		User user = userRepository.findByEmailIgnoreCase(email)
				.orElseThrow(() -> new IllegalStateException("ログインユーザーが見つかりません: " + email));

		HttpSession session = request.getSession();
		session.setAttribute("LOGIN_USER_ID", user.getUserId());

		response.sendRedirect("/home");
	}
}