package com.example.shibakamio2026.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CsvImportBatch implements java.io.Serializable {
	private static final long serialVersionUID = 1L;

	private List<CsvImportRow> rows;
}