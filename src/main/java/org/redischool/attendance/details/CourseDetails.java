package org.redischool.attendance.details;

import java.util.Date;
import java.util.List;

public class CourseDetails {
    private String name;
    private List<String> students;
    private List<String> formattedDates;
    private List<Date> javaDates;

    public CourseDetails(String name, List<String> students, List<String> formattedDates, List<Date> javaDates) {
        this.name = name;
        this.students = students;
        this.formattedDates = formattedDates;
        this.javaDates = javaDates;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getStudents() {
        return students;
    }

    public void setStudents(List<String> students) {
        this.students = students;
    }

    public List<String> getFormattedDates() {
        return formattedDates;
    }

    public void setFormattedDates(List<String> formattedDates) {
        this.formattedDates = formattedDates;
    }

    public List<Date> getJavaDates() {
        return javaDates;
    }

    public void setJavaDates(List<Date> javaDates) {
        this.javaDates = javaDates;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CourseDetails that = (CourseDetails) o;

        if (!name.equals(that.name)) return false;
        if (!students.equals(that.students)) return false;
        if (!formattedDates.equals(that.formattedDates)) return false;
        return javaDates.equals(that.javaDates);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + students.hashCode();
        result = 31 * result + formattedDates.hashCode();
        result = 31 * result + javaDates.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "CourseDetails{" +
                "name='" + name + '\'' +
                ", students=" + students +
                ", formattedDates=" + formattedDates +
                ", javaDates=" + javaDates +
                '}';
    }
}
