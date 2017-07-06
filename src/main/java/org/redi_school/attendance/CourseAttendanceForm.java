package org.redi_school.attendance;

import java.util.Map;

public class CourseAttendanceForm {

    private Map<String, String> attendances;

    private String date;

    private String courseName;

    public Map<String, String> getAttendances() {
        return attendances;
    }

    public void setAttendances(Map<String, String> attendances) {
        this.attendances = attendances;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }
}
