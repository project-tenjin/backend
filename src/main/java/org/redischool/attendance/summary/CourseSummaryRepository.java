package org.redischool.attendance.summary;

import com.google.api.services.sheets.v4.model.Sheet;
import org.redischool.attendance.spreadsheet.GoogleSheetsApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
public class CourseSummaryRepository {

	private static final String FILTER_OUT_SHEET_CHAR = "*";

	private GoogleSheetsApi googleSheetsApi;
	private String spreadsheetId;

	@Autowired
	public CourseSummaryRepository(
			GoogleSheetsApi googleSheetsApi,
			@Value("${google.spreadsheet.id}") String spreadsheetId) {

		this.googleSheetsApi = googleSheetsApi;
		this.spreadsheetId = spreadsheetId;
	}

	public List<CourseSummary> getCourses() {
		List<Sheet> sheets = googleSheetsApi.getSheets(spreadsheetId);
		return sheets.stream()
				.filter(this::mustBeShown)
				.map(this::sheetToCourseSummary)
				.collect(toList());
	}

	private boolean mustBeShown(Sheet sheet) {
		return !sheet.getProperties().getTitle().contains(FILTER_OUT_SHEET_CHAR);
	}

	private CourseSummary sheetToCourseSummary(Sheet sheet) {
		return new CourseSummary(
				sheet.getProperties().getSheetId(),
				sheet.getProperties().getTitle()
		);
	}
}
