package com.example.shibakamio2026.exception;

/**
 * 資産管理モジュール（asset-module）にあるものと同一内容です。実リポジトリには
 * 1つだけ存在すればよいので、統合時はどちらか一方を残してください。
 *
 * 指定されたIDのリソースが存在しない、または他ユーザーのリソースにアクセスしようとした場合にスローする例外。
 */
public class ResourceNotFoundException extends RuntimeException {
	public ResourceNotFoundException(String message) {
		super(message);
	}
}
