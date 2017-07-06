package org.redi_school.attendance;

import com.google.api.services.sheets.v4.model.Sheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.lang.StrictMath.max;

@Service
public class CoursesRepository {

    private static final String NON_COURSE_SHEET_NAME = "attendance key";
    private static final int ADDITIONAL_DATE_FIELDS_COUNT = 3;
    private static final int DATE_FIELD_START_INDEX = 2;
    private static final int DATE_ROW_INDEX = 1;
    private static final int STUDENT_NAME_COLUMN_INDEX = 1;
    private static final int ATTENDANCE_DATA_ROW_START_INDEX = 4;

    private static String ALL_DATA_RANGE = "A:ZZ";
    private static int HEADER_ROW_COUNT = 3;

    private final Environment environment;
    private final SpreadsheetColumnNameMapper helper;
    private GoogleSheetsApi googleSheetsApi;
    private String spreadsheetId;

    @Autowired
    CoursesRepository(GoogleSheetsApi googleSheetsApi, Environment environment) {
        this.googleSheetsApi = googleSheetsApi;
        this.environment = environment;
        this.helper = new SpreadsheetColumnNameMapper();
        spreadsheetId = this.environment.getProperty("google.spreadsheet.id");
    }

    List<CourseSummary> getCourses() {
        List<Sheet> sheets = this.googleSheetsApi.getSheets(spreadsheetId);
        return sheets.stream()
                .filter((sheet) -> (!sheet.getProperties().getTitle().toLowerCase()
                        .equals(NON_COURSE_SHEET_NAME.toLowerCase())))
                .map((sheet) -> new CourseSummary(
                        sheet.getProperties().getSheetId(),
                        sheet.getProperties().getTitle()
                ))
                .collect(Collectors.toList());
    }

    CourseDetails getCourseDetails(String courseName) {
        List<List<Object>> sheetData = this.googleSheetsApi.getRange(spreadsheetId, courseName, ALL_DATA_RANGE);

        List<String> students = getStudentNames(sheetData);
        List<String> dates = getDates(sheetData);
        Map<String, Map<String, String>> attendances = getAttendances(sheetData);

        return new CourseDetails(courseName, students, dates, attendances);
    }

    void updateCourseData(String courseName, String date, Map<String, String> newData) {
        if (newData.size() == 0) {
            return;
        }

        List<List<Object>> spreadsheetData = this.googleSheetsApi.getRange(spreadsheetId, courseName, ALL_DATA_RANGE);
        String range = calculateRangeForUpdate(spreadsheetData, date);
        List<List<Object>> dataToWrite = diffData(spreadsheetData, newData, date);

        this.googleSheetsApi.updateDataRange(spreadsheetId, courseName, range, dataToWrite);
    }

    private List<String> getStudentNames(List<List<Object>> sheetData) {
        return sheetData.stream()
                .skip(HEADER_ROW_COUNT)
                .map((row) -> row.get(STUDENT_NAME_COLUMN_INDEX).toString())
                .filter((student) -> !student.equals(""))
                .collect(Collectors.toList());
    }

    private List<String> getDates(List<List<Object>> sheetData) {
        List<String> rawDates = sheetData.get(DATE_ROW_INDEX).stream()
                .skip(DATE_FIELD_START_INDEX)
                .map(Object::toString)
                .collect(Collectors.toList());
        return rawDates.subList(0, max(0, rawDates.size() - ADDITIONAL_DATE_FIELDS_COUNT));
    }

    private Map<String, Map<String, String>> getAttendances(List<List<Object>> sheetData) {
        Map<String, Map<String, String>> datesToStudentToAttendance = new HashMap<>();
        List<String> studentNames = this.getStudentNames(sheetData);
        this.getDates(sheetData).forEach((date) -> {
            int dateColumnIndex = columnIndexForDate(sheetData, date);
            datesToStudentToAttendance.put(date, new HashMap<>());
            sheetData.stream()
                    .skip(ATTENDANCE_DATA_ROW_START_INDEX)
                    .map((List<Object> row) -> { row.get(dateColumnIndex).toString(); });
        });


        return null;
    }

    private List<List<Object>> diffData(List<List<Object>> spreadsheetData, Map<String, String> newData, String date) {
        int columnIndexForDate = columnIndexForDate(spreadsheetData, date);

        return spreadsheetData.stream()
                .skip(HEADER_ROW_COUNT)
                .filter(CoursesRepository::studentsWithoutName)
                .map(row -> newStudentAttendanceValue(row, columnIndexForDate, newData))
                .collect(Collectors.toList());
    }

    private int columnIndexForDate(List<List<Object>> spreadsheetData, String date) {
        return DATE_FIELD_START_INDEX + getDates(spreadsheetData).indexOf(date);
    }

    private List<Object> newStudentAttendanceValue(List<Object> rowWithStudentData, int columnIndexForDate, Map<String, String> newData) {
        String studentName = rowWithStudentData.get(STUDENT_NAME_COLUMN_INDEX).toString();
        String oldAttendanceValue = rowWithStudentData.get(columnIndexForDate).toString();
        String newAttendanceValue = newData.getOrDefault(studentName, oldAttendanceValue);
        return Collections.singletonList((Object) newAttendanceValue);
    }

    private static boolean studentsWithoutName(List<Object> rowWithStudentData) {
        return !rowWithStudentData.get(STUDENT_NAME_COLUMN_INDEX).equals("");
    }

    private String calculateRangeForUpdate(List<List<Object>> rawData, String date) {
        List<String> dates = getDates(rawData);
        int columnOfDate = dates.indexOf(date) + 1; // +1 since spreadsheet is 1 indexed
        int numberOfStudends = getStudentNames(rawData).size();

        int startIndexForStudentData = HEADER_ROW_COUNT + 1;
        int endIndexForStudentData = startIndexForStudentData + numberOfStudends;

        String columnLetter = this.helper.columnIndexToLetter(DATE_FIELD_START_INDEX + columnOfDate);
        return columnLetter + startIndexForStudentData + ":" + columnLetter + endIndexForStudentData;
    }
}
