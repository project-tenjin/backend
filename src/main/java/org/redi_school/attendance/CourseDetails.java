package org.redi_school.attendance;

import java.util.List;

public class CourseDetails {
    private String name;
    private List<String> students;

    public CourseDetails(String name, List<String> students) {
        this.name = name;
        this.students = students;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CourseDetails that = (CourseDetails) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        return students != null ? students.equals(that.students) : that.students == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (students != null ? students.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CourseDetails{" +
                "name='" + name + '\'' +
                ", students=" + students +
                '}';
    }
}
