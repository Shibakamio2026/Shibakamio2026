package com.example.shibakamio2026.exception;

public class InvalidCsvFormatException extends RuntimeException {
	public InvalidCsvFormatException(String message) {
		super(message);
	}
}