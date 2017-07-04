package org.redi_school.attendance;

import com.google.api.services.sheets.v4.model.Sheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static java.lang.StrictMath.max;

@Service
public class CoursesRepository {

    private static final String NON_COURSE_SHEET_NAME = "attendance key";
    private static final int ADDITIONAL_DATE_FIELDS_COUNT = 3;
    private static final int DATE_FIELD_START_INDEX = 2;
    private static String ALL_DATA_RANGE = "A:ZZ";
    private static int HEADER_ROW_COUNT = 3;

    private final Environment environment;
    private GoogleSheetsApi googleSheetsApi;
    private String spreadsheetId;

    @Autowired
    public CoursesRepository(GoogleSheetsApi googleSheetsApi, Environment environment) {
        this.googleSheetsApi = googleSheetsApi;
        this.environment = environment;
        spreadsheetId = this.environment.getProperty("google.spreadsheet.id");
    }

    public List<CourseSummary> getCourses() {
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

    public CourseDetails getCourseDetails(String courseName) {
        List<List<Object>> sheetData = this.googleSheetsApi.getRange(spreadsheetId, courseName, ALL_DATA_RANGE);

        List<String> students = getStudentNames(sheetData);
        List<String> dates = getDates(sheetData);

        return new CourseDetails(courseName, students, dates);
    }

    private List<String> getStudentNames(List<List<Object>> sheetData) {
        return sheetData.stream()
                    .skip(HEADER_ROW_COUNT)
                    .map((row) -> row.get(1).toString())
                    .filter((student) -> !student.equals(""))
                    .collect(Collectors.toList());
    }

    private List<String> getDates(List<List<Object>> sheetData) {
        List<String> rawDates = sheetData.get(1).stream()
                .skip(DATE_FIELD_START_INDEX)
                .map(Object::toString)
                .collect(Collectors.toList());
        return rawDates.subList(0, max(0, rawDates.size() - ADDITIONAL_DATE_FIELDS_COUNT));
    }
}
