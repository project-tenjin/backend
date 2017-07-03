package org.redi_school.attendance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CoursesRepository {

    private static final String NON_COURSE_SHEET_NAME = "attendance key";

    private final Environment environment;
    private GoogleSheetsApi googleSheetsApi;
    private String spreadsheetId;
    private String STUDENTS_COLUMN_RANGE = "B:B";


    @Autowired
    public CoursesRepository(GoogleSheetsApi googleSheetsApi, Environment environment) {
        this.googleSheetsApi = googleSheetsApi;
        this.environment = environment;
        spreadsheetId = this.environment.getProperty("google.spreadsheet.id");
    }

    public List<Sheet> getCourses() {
        List<Sheet> sheetNames = this.googleSheetsApi.getSheets(spreadsheetId);
        return sheetNames.stream()
                .filter((sheet) -> (!sheet.getName().toLowerCase().equals(NON_COURSE_SHEET_NAME.toLowerCase())))
                .collect(Collectors.toList());
    }

    public List<String> getStudentsForCourse(String courseId) {
        List<List<Object>> studentsColumn = this.googleSheetsApi.getRange(spreadsheetId, courseId, STUDENTS_COLUMN_RANGE);
        return studentsColumn.stream()
                .skip(3)
                .map((row) -> row.size() == 0 ? "" : row.get(0).toString())
                .collect(Collectors.toList());
    }
}
