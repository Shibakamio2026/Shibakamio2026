package com.example.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupForm {

	@NotBlank(message = "入力してください")
	private String userName;

	@NotBlank(message = "入力してください")
	@Email(message = "正しいメールアドレスを入力してください")
	private String email;

	@NotBlank(message = "入力してください")
	private String password;

	@NotBlank(message = "入力してください")
	private String confirmPassword;
}