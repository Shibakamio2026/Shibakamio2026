package com.example.shibakamio2026.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * パスワードのハッシュ化・照合に使用する PasswordEncoder の Bean 定義。
 * G17（アカウント削除）で、削除前の本人確認のためにパスワード照合に使用する。
 *
 * 【認証チームとの前提】
 * pom.xml に spring-security-crypto（または spring-boot-starter-security）を
 * 追加する必要があります。認証チームがログイン・新規登録（BCryptでのハッシュ化）
 * のために同様の PasswordEncoder Bean を定義する場合、Bean定義が重複しないよう
 * どちらか一方に統一してください。
 */
@Configuration
public class SecurityBeansConfig {

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
