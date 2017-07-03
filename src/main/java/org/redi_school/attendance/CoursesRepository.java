package org.redi_school.attendance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CoursesRepository {

    private static final String NON_COURSE_SHEET_NAME = "Attendance key";
    private GoogleSheetsApi googleSheetsApi;

    @Autowired
    public CoursesRepository(GoogleSheetsApi googleSheetsApi) {
        this.googleSheetsApi = googleSheetsApi;
    }

    public List<String> getCourses() {
        List<String> sheetNames = this.googleSheetsApi.getSheetNames();
        return sheetNames.stream()
                .filter((name) -> (!name.equals(NON_COURSE_SHEET_NAME)))
                .collect(Collectors.toList());
    }

}
