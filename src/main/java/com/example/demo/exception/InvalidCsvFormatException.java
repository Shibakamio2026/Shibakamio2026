package com.example.demo.exception;

public class InvalidCsvFormatException extends RuntimeException {
	public InvalidCsvFormatException(String message) {
		super(message);
	}
}