package org.redi_school.attendance;

import java.util.List;

public class CourseDetails {
    private String name;
    private List<String> students;
    private List<String> dates;

    public CourseDetails(String name, List<String> students, List<String> dates) {
        this.name = name;
        this.students = students;
        this.dates = dates;
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

    public List<String> getDates() {
        return dates;
    }

    public void setDates(List<String> dates) {
        this.dates = dates;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CourseDetails that = (CourseDetails) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (students != null ? !students.equals(that.students) : that.students != null) return false;
        return dates != null ? dates.equals(that.dates) : that.dates == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (students != null ? students.hashCode() : 0);
        result = 31 * result + (dates != null ? dates.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CourseDetails{" +
                "name='" + name + '\'' +
                ", students=" + students +
                ", dates=" + dates +
                '}';
    }
}
