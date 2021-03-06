package org.redischool.attendance.details;

import com.google.api.services.sheets.v4.model.CellData;
import com.google.api.services.sheets.v4.model.ExtendedValue;
import com.google.api.services.sheets.v4.model.GridData;
import com.google.api.services.sheets.v4.model.RowData;
import org.redischool.attendance.spreadsheet.GoogleSheetsApi;
import org.redischool.attendance.spreadsheet.SpreadsheetColumnNameMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

import static java.lang.StrictMath.max;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Service
public class CourseDetailsRepository {

    private static final int ADDITIONAL_DATE_FIELDS_COUNT = 4;
    private static final int DATE_FIELD_START_INDEX = 2;
    private static final int DATE_ROW_INDEX = 1;
    private static final int STUDENT_NAME_COLUMN_INDEX = 1;

    private static final String ALL_DATA_RANGE = "A:ZZ";
    private static final int HEADER_ROW_COUNT = 3;

    private static final String FILTER_OUT_SHEET_CHAR = "*";

    private final SpreadsheetColumnNameMapper spreadsheetColumnNameMapper;
    private final GoogleSheetsApi googleSheetsApi;
    private final String spreadsheetId;

    @Autowired
    public CourseDetailsRepository(
            GoogleSheetsApi googleSheetsApi,
            @Value("${google.spreadsheet.id}") String spreadsheetId) {

        this.googleSheetsApi = googleSheetsApi;
        this.spreadsheetColumnNameMapper = new SpreadsheetColumnNameMapper();
        this.spreadsheetId = spreadsheetId;
    }

    public CourseDetails getCourseDetails(String courseName) {
        List<List<Object>> sheetData = getSheetValues(courseName);
        GridData gridData = getGridData(courseName);

        List<String> students = getStudentNamesAttendingCourse(sheetData);
        List<String> formattedDates = getFormattedDates(sheetData);
        List<Date> javaDates = getJavaDates(gridData);

        return new CourseDetails(courseName, students, formattedDates, javaDates);
    }

    public Map<String, String> getAttendance(String courseName, String date) {
        List<List<Object>> sheetData = getSheetValues(courseName);

        int columnIndexForDate = columnIndexForDate(sheetData, date);

        return sheetData.stream()
                .skip(HEADER_ROW_COUNT)
                .filter(this::hasStudent)
                .collect(toMap(
                        row -> fetchStudent(row),
                        row -> row.get(columnIndexForDate).toString()
        ));
    }

    public void updateAttendance(String courseName, String date, Map<String, String> newAttendance) throws IllegalArgumentException {
        if (newAttendance.isEmpty()) return;

        List<List<Object>> spreadsheetData = getSheetValues(courseName);

        String range = calculateRangeForUpdate(spreadsheetData, date);

        List<List<Object>> dataToWrite = diffData(spreadsheetData, newAttendance, date);

        googleSheetsApi.updateDataRange(spreadsheetId, courseName, range, dataToWrite);
    }

    private List<List<Object>> getSheetValues(String courseName) {
        return googleSheetsApi.getRange(spreadsheetId, courseName, ALL_DATA_RANGE);
    }

    private GridData getGridData(String courseName) {
        return googleSheetsApi.getGridData(spreadsheetId, courseName, ALL_DATA_RANGE);
    }

    private boolean hasStudent(List<Object> row) {
        return !fetchStudent(row).equals("");
    }

    private boolean mustBeShown(String studentName) {
        return !studentName.contains(FILTER_OUT_SHEET_CHAR);
    }

    private String fetchStudent(List<Object> row) {
        return row.get(STUDENT_NAME_COLUMN_INDEX).toString();
    }

    private List<String> getStudentNamesAttendingCourse(List<List<Object>> sheetData) {
        return sheetData.stream()
                .skip(HEADER_ROW_COUNT)
                .filter(this::hasStudent)
                .map(this::fetchStudent)
                .filter(this::mustBeShown)
                .collect(toList());
    }

    private List<String> getFormattedDates(List<List<Object>> sheetData) {
        List<String> rawDates = sheetData.get(DATE_ROW_INDEX).stream()
                .skip(DATE_FIELD_START_INDEX)
                .map(Object::toString)
                .filter(o -> !o.equals(""))
                .collect(toList());

        return rawDates.subList(0, max(0, rawDates.size() - ADDITIONAL_DATE_FIELDS_COUNT));
    }

    private List<Date> getJavaDates(GridData gridData) {
        if (gridData == null) {
            // Early opt-out; really just here to make testing work
            // GridData is `final`, so we can not mock it. We'd have to create
            // a whole sample GridData from hand...
            return Collections.emptyList();
        }

        RowData rowData = gridData.getRowData().get(DATE_ROW_INDEX);
        List<CellData> allCellData = rowData.getValues();
        int max = max(DATE_FIELD_START_INDEX, allCellData.size() - ADDITIONAL_DATE_FIELDS_COUNT);
        List<CellData> dateCellData = allCellData.subList(DATE_FIELD_START_INDEX, max);

        return dateCellData.stream()
                .map(CellData::getEffectiveValue)
                .filter(Objects::nonNull)
                .map(ExtendedValue::getNumberValue)
                .map(GoogleSheetsApi::dateFromSerial)
                .collect(toList());
    }

    private List<List<Object>> diffData(List<List<Object>> spreadsheetData, Map<String, String> newData, String date) {
        int columnIndexForDate = columnIndexForDate(spreadsheetData, date);

        return spreadsheetData.stream()
                .skip(HEADER_ROW_COUNT)
                .filter(this::hasStudent)
                .map(row -> newStudentAttendanceValue(row, columnIndexForDate, newData))
                .collect(toList());
    }

    private List<Object> newStudentAttendanceValue(List<Object> rowWithStudentData, int columnIndexForDate, Map<String, String> newData) {
        String studentName = fetchStudent(rowWithStudentData);
        String oldAttendanceValue = rowWithStudentData.get(columnIndexForDate).toString();

        String newAttendanceValue = newData.getOrDefault(studentName, oldAttendanceValue);

        if (oldAttendanceValue.equals("E") && newAttendanceValue.equals("U")) {
            // If a student is already excused in the spreadsheet, don't override it with unexcused!
            return Collections.singletonList(oldAttendanceValue);
        }

        return Collections.singletonList(newAttendanceValue);
    }

    private String calculateRangeForUpdate(List<List<Object>> rawData, String date) throws IllegalArgumentException {
        List<String> dates = getFormattedDates(rawData);
        int dateIndex = dates.indexOf(date);

        if(dateIndex == -1) throw new IllegalArgumentException("Please select a date.");

        int columnOfDate = dateIndex + 1; // +1 since spreadsheet is 1 indexed
        int numberOfStudents = getAllStudentNames(rawData).size();

        int startIndexForStudentData = HEADER_ROW_COUNT + 1;
        int endIndexForStudentData = startIndexForStudentData + numberOfStudents;

        String columnLetter = spreadsheetColumnNameMapper.columnIndexToLetter(DATE_FIELD_START_INDEX + columnOfDate);

        return columnLetter + startIndexForStudentData + ":" + columnLetter + endIndexForStudentData;
    }

    private int columnIndexForDate(List<List<Object>> sheetData, String date) {
        return DATE_FIELD_START_INDEX + getFormattedDates(sheetData).indexOf(date);
    }

    private List<String> getAllStudentNames(List<List<Object>> sheetData) {
        return sheetData.stream()
                .skip(HEADER_ROW_COUNT)
                .filter(this::hasStudent)
                .map(this::fetchStudent)
                .collect(toList());
    }

}
