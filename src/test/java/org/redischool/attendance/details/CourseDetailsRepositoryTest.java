package org.redischool.attendance.details;

import com.mscharhag.oleaster.runner.OleasterRunner;
import org.hamcrest.Matchers;
import org.junit.runner.RunWith;
import org.redischool.attendance.spreadsheet.GoogleSheetsApi;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.mscharhag.oleaster.runner.StaticRunnerSupport.*;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@RunWith(OleasterRunner.class)
public class CourseDetailsRepositoryTest {

    private CourseDetailsRepository courseDetailsRepository;
    private GoogleSheetsApi googleSheetsApi;

    {
        describe("CourseDetailsRepository", () -> {
            final String spreadsheetId = "meehp";

            beforeEach(() -> {
                googleSheetsApi = mock(GoogleSheetsApi.class);

                courseDetailsRepository = new CourseDetailsRepository(googleSheetsApi, spreadsheetId);
            });

            describe("course details", () -> {
                it("returns details about a course and strips out non-date fields from my dates", () -> {
                    String courseName = "CourseSummary 1";
                    List<String> students = asList("Student 1", "Student 2");
                    List<String> dates = asList("3/31", "4/12", "5/18");

                    List<List<Object>> sheetData = asList(
                            asList(""),
                            asList("", "", dates.get(0), dates.get(1), dates.get(2), "Present", "Late", "Excused absence", "Unexcused absence"),
                            asList(""), // Day of week
                            asList("", students.get(0)),
                            asList("", students.get(1))
                    );

                    given(googleSheetsApi.getRange(spreadsheetId, courseName, "A:ZZ"))
                            .willReturn(sheetData);

                    assertThat(courseDetailsRepository.getCourseDetails(courseName))
                            .isEqualTo(new CourseDetails(courseName, students, dates));
                });

                it("returns details about a course and filters out empty student rows", () -> {
                    String courseName = "CourseSummary 1";
                    List<String> students = asList("Student 1", "Student 2");

                    List<List<Object>> sheetData = asList(
                            asList(""),
                            asList(""), // Dates
                            asList(""), // Day of week
                            asList("", "Student 1"),
                            asList("", "Student 2"),
                            asList("", ""), // Sometimes there are empty cells in the spreadsheet
                            asList("", "")
                    );

                    given(googleSheetsApi.getRange(spreadsheetId, courseName, "A:ZZ"))
                            .willReturn(sheetData);

                    assertThat(courseDetailsRepository.getCourseDetails(courseName))
                            .isEqualTo(new CourseDetails(courseName, students, Collections.emptyList()));
                });

                it("returns attendance for a particular course and date", () -> {
                    String courseName = "Unicorns";
                    List<String> dates = asList("3/31", "4/20", "5/18");
                    List<String> students = asList("Student 1", "Student 2", "Student 3", "Student 4");

                    List<List<Object>> sheetData = asList(
                            asList(""),
                            asList("", "", dates.get(0), dates.get(1), dates.get(2), "Present", "Late", "Excused absence", "Unexcused absence"),
                            asList(""), // Day of week
                            asList("", students.get(0), "P"),
                            asList("", students.get(1), "L"),
                            asList("", students.get(2), "L"),
                            asList("", students.get(3), "U")
                    );

                    given(googleSheetsApi.getRange(spreadsheetId, courseName, "A:ZZ"))
                            .willReturn(sheetData);

                    Map<String, String> attendance = courseDetailsRepository.getAttendance(courseName, dates.get(0));

                    assertThat(attendance.get(students.get(0))).isEqualTo("P");
                    assertThat(attendance.get(students.get(1))).isEqualTo("L");
                    assertThat(attendance.get(students.get(2))).isEqualTo("L");
                    assertThat(attendance.get(students.get(3))).isEqualTo("U");
                });
            });

            describe("update course", () -> {
                it("doesn't do anything with empty update data", () -> {
                    courseDetailsRepository.updateAttendance("1", "", new HashMap<String, String>());
                    verifyZeroInteractions(googleSheetsApi);
                });

                it("throws an error with no date", () -> {
                    try {
                        String courseName = "Unicorns";

                        List<String> dates = asList("3/31");
                        List<String> students = asList("Student A");
                        List<List<Object>> sheetData = asList(
                                asList(""),
                                asList("", "", dates.get(0), "Present", "Late", "Excused absence", "Unexcused absence"),
                                asList(""), // Day of week
                                asList("", students.get(0), "", "")
                        );
                        given(googleSheetsApi.getRange(spreadsheetId, courseName, "A:ZZ")).willReturn(sheetData);


                        courseDetailsRepository.updateAttendance("Unicorns", "WRONG_DATE", new HashMap<String, String>() {{ put("Student A", "P");}});
                        assertThat(true).isFalse();
                    } catch(IllegalArgumentException e){
                        assertThat(e.getMessage()).isEqualTo("Please select a date.");
                    }
                });

                it("updates the course attendance data", () -> {

                    String courseName = "Unicorns";
                    List<String> dates = asList("3/31", "4/20", "5/18");
                    List<String> students = asList("Student 1", "[D] Amer Afoura", "Student 3", "[T] Apratim Choudhury");

                    List<List<Object>> sheetData = asList(
                            asList(""),
                            asList("", "", dates.get(0), dates.get(1), dates.get(2), "Present", "Late", "Excused absence", "Unexcused absence"),
                            asList(""), // Day of week
                            asList("", students.get(0), "", "P"),
                            asList("", students.get(1), "", ""),
                            asList("", students.get(2), "", ""),
                            asList("", students.get(3), "", "U")
                    );

                    HashMap<String, String> updateData = new HashMap<String, String>() {{
                        put(students.get(1), "E");
                        put(students.get(3), "P");
                    }};

                    // D corresponds with the date, "4/20"
                    String range = "D4:D8";

                    List<List<Object>> dataToWriteToSpreadsheet = asList(
                            Collections.singletonList("P"),
                            Collections.singletonList("E"),
                            Collections.singletonList(""),
                            Collections.singletonList("P")
                    );

                    given(googleSheetsApi.getRange(spreadsheetId, courseName, "A:ZZ")).willReturn(sheetData);

                    courseDetailsRepository.updateAttendance(courseName, dates.get(1), updateData);

                    verify(googleSheetsApi).updateDataRange(spreadsheetId, courseName, range, dataToWriteToSpreadsheet);
                });

                it("doesnt update column if a student alrady have E attendance status", () -> {

                    String courseName = "Unicorns";
                    List<String> dates = asList("3/31", "4/20", "5/18");
                    List<String> students = asList("Student 1", "Student 2", "Student 3", "Student 4");

                    List<List<Object>> sheetData = asList(
                            asList(""),
                            asList("", "", dates.get(0), dates.get(1), dates.get(2), "Present", "Late", "Excused absence", "Unexcused absence"),
                            asList(""), // Day of week
                            asList("", students.get(0), "", ""),
                            asList("", students.get(1), "", "E"),
                            asList("", students.get(2), "", "E"),
                            asList("", students.get(3), "", "")
                    );

                    HashMap<String, String> updateData = new HashMap<String, String>() {{
                        put(students.get(0), "P");
                        put(students.get(1), "U");
                        put(students.get(2), "L");
                        put(students.get(3), "L");
                    }};

                    // D corresponds with the date, "4/20"
                    String range = "D4:D8";

                    List<List<Object>> dataToWriteToSpreadsheet = asList(
                            Collections.singletonList("P"),
                            Collections.singletonList("E"),
                            Collections.singletonList("L"),
                            Collections.singletonList("L")
                    );

                    given(googleSheetsApi.getRange(spreadsheetId, courseName, "A:ZZ")).willReturn(sheetData);

                    courseDetailsRepository.updateAttendance(courseName, dates.get(1), updateData);

                    verify(googleSheetsApi).updateDataRange(spreadsheetId, courseName, range, dataToWriteToSpreadsheet);
                });
            });

        });
    }
}
