package org.redi_school.attendance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CoursesRepository {

    private GoogleSheetsApi googleSheetsApi;

    @Autowired
    public CoursesRepository(GoogleSheetsApi googleSheetsApi) {
        this.googleSheetsApi = googleSheetsApi;
    }

    public List<String> getCourses() {
        return this.googleSheetsApi.getSheetNames();
    }

}
