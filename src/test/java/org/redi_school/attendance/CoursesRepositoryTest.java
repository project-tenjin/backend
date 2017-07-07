package org.redi_school.attendance;

import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.SheetProperties;
import com.mscharhag.oleaster.runner.OleasterRunner;
import org.junit.runner.RunWith;
import org.springframework.mock.env.MockEnvironment;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static com.mscharhag.oleaster.runner.StaticRunnerSupport.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@RunWith(OleasterRunner.class)
public class CoursesRepositoryTest {
    CoursesRepository coursesRepository;
    GoogleSheetsApi googleSheetsApi;
    MockEnvironment mockEnvironment;

    Sheet buildSheet(int id, String name) {
        Sheet sheet = new Sheet();
        SheetProperties properties = new SheetProperties();
        properties.setSheetId(id);
        properties.setTitle(name);
        sheet.setProperties(properties);

        return sheet;
    }

    {
        describe("CoursesRepository", () -> {
            final String spreadsheetId = "meehp";

            beforeEach(() -> {
                this.mockEnvironment = new MockEnvironment();
                mockEnvironment.setProperty("google.spreadsheet.id", spreadsheetId);

                this.googleSheetsApi = mock(GoogleSheetsApi.class);
                this.coursesRepository = new CoursesRepository(googleSheetsApi, mockEnvironment);
            });

            describe("fetching information from the courses Google spreadsheet", () -> {
                describe("when there are sheets in the targeted spreadsheet", () -> {
                    it("returns the sheet names", () -> {
                        given(this.googleSheetsApi.getSheets(spreadsheetId))
                                .willReturn(Arrays.asList(buildSheet(0, "CourseSummary A"), buildSheet(1, "CourseSummary B")));

                        assertThat(this.coursesRepository.getCourses())
                                .isEqualTo(Arrays.asList(new CourseSummary(0, "CourseSummary A"), new CourseSummary(1, "CourseSummary B")));
                    });

                    it("filters out non-course sheets with asterisk in the name", () -> {
                        given(this.googleSheetsApi.getSheets(spreadsheetId))
                                .willReturn(Arrays.asList(
                                        buildSheet(0, "*Attendance key"),
                                        buildSheet(1, "CourseSummary A"),
                                        buildSheet(2, "CourseSummary B"),
                                        buildSheet(3, "Hide me*"),
                                        buildSheet(4, "CourseSummary C")));

                        assertThat(this.coursesRepository.getCourses())
                                .isEqualTo(Arrays.asList(
                                        new CourseSummary(1, "CourseSummary A"),
                                        new CourseSummary(2, "CourseSummary B"),
                                        new CourseSummary(4, "CourseSummary C")));
                    });
                });

                describe("when there are no sheets in the targeted spreadsheet", () -> {
                    beforeEach(() -> {
                        given(this.googleSheetsApi.getSheets(spreadsheetId))
                                .willReturn(Arrays.asList());
                    });

                    it("returns the sheet names", () -> {
                        assertThat(this.coursesRepository.getCourses())
                                .isEmpty();
                    });
                });
            });

            describe("course details", () -> {
                it("returns details about a course and strips out non-date fields from my dates", () -> {
                    String courseName = "CourseSummary 1";
                    List<String> students = Arrays.asList("Student 1", "Student 2");
                    List<String> dates = Arrays.asList("3/31", "4/12", "5/18");

                    List<List<Object>> sheetData = Arrays.asList(
                            Arrays.asList(""),
                            Arrays.asList("", "", dates.get(0), dates.get(1), dates.get(2), "Present", "Late", "Excused absence", "Unexcused absence"),
                            Arrays.asList(""), // Day of week
                            Arrays.asList("", students.get(0)),
                            Arrays.asList("", students.get(1))
                    );

                    given(this.googleSheetsApi.getRange(spreadsheetId, courseName, "A:ZZ"))
                            .willReturn(sheetData);

                    assertThat(this.coursesRepository.getCourseDetails(courseName))
                            .isEqualTo(new CourseDetails(courseName, students, dates));
                });

                it("returns details about a course and filters out empty student rows", () -> {
                    String courseName = "CourseSummary 1";
                    List<String> students = Arrays.asList("Student 1", "Student 2");

                    List<List<Object>> sheetData = Arrays.asList(
                            Arrays.asList(""),
                            Arrays.asList(""), // Dates
                            Arrays.asList(""), // Day of week
                            Arrays.asList("", "Student 1"),
                            Arrays.asList("", "Student 2"),
                            Arrays.asList("", ""), // Sometimes there are empty cells in the spreadsheet
                            Arrays.asList("", "")
                    );

                    given(this.googleSheetsApi.getRange(spreadsheetId, courseName, "A:ZZ"))
                            .willReturn(sheetData);

                    assertThat(this.coursesRepository.getCourseDetails(courseName))
                            .isEqualTo(new CourseDetails(courseName, students, Collections.emptyList()));
                });
            });

            describe("update course", () -> {
                it("doesn't do anything with empty update data", () -> {
                    this.coursesRepository.updateCourseData("1", "", new HashMap<String, String>());
                    verifyZeroInteractions(this.googleSheetsApi);
                });

                it("updates the course attendance data", () -> {

                    String courseName = "Unicorns";
                    List<String> dates = Arrays.asList("3/31", "4/20", "5/18");
                    List<String> students = Arrays.asList("Student 1", "[D] Amer Afoura", "Student 3", "[T] Apratim Choudhury");

                    List<List<Object>> sheetData = Arrays.asList(
                            Arrays.asList(""),
                            Arrays.asList("", "", dates.get(0), dates.get(1), dates.get(2), "Present", "Late", "Excused absence", "Unexcused absence"),
                            Arrays.asList(""), // Day of week
                            Arrays.asList("", students.get(0), "", "P"),
                            Arrays.asList("", students.get(1), "", ""),
                            Arrays.asList("", students.get(2), "", ""),
                            Arrays.asList("", students.get(3), "", "U")
                    );

                    HashMap<String, String> updateData = new HashMap<String, String>() {{
                        put(students.get(1), "E");
                        put(students.get(3), "P");
                    }};

                    // D corresponds with the date, "4/20"
                    String range = "D4:D8";

                    List<List<Object>> dataToWriteToSpreadsheet = Arrays.asList(
                            Collections.singletonList("P"),
                            Collections.singletonList("E"),
                            Collections.singletonList(""),
                            Collections.singletonList("P")
                    );

                    given(this.googleSheetsApi.getRange(spreadsheetId, courseName, "A:ZZ")).willReturn(sheetData);

                    this.coursesRepository.updateCourseData(courseName, dates.get(1), updateData);

                    verify(this.googleSheetsApi).updateDataRange(spreadsheetId, courseName, range, dataToWriteToSpreadsheet);
                });

                it("doesnt update column if a student alrady have E attendance status", () -> {

                    String courseName = "Unicorns";
                    List<String> dates = Arrays.asList("3/31", "4/20", "5/18");
                    List<String> students = Arrays.asList("Student 1", "Student 2", "Student 3", "Student 4");

                    List<List<Object>> sheetData = Arrays.asList(
                            Arrays.asList(""),
                            Arrays.asList("", "", dates.get(0), dates.get(1), dates.get(2), "Present", "Late", "Excused absence", "Unexcused absence"),
                            Arrays.asList(""), // Day of week
                            Arrays.asList("", students.get(0), "", ""),
                            Arrays.asList("", students.get(1), "", "E"),
                            Arrays.asList("", students.get(2), "", "E"),
                            Arrays.asList("", students.get(3), "", "")
                    );

                    HashMap<String, String> updateData = new HashMap<String, String>() {{
                        put(students.get(0), "P");
                        put(students.get(1), "U");
                        put(students.get(2), "L");
                        put(students.get(3), "L");
                    }};

                    // D corresponds with the date, "4/20"
                    String range = "D4:D8";

                    List<List<Object>> dataToWriteToSpreadsheet = Arrays.asList(
                            Collections.singletonList("P"),
                            Collections.singletonList("E"),
                            Collections.singletonList("L"),
                            Collections.singletonList("L")
                    );

                    given(this.googleSheetsApi.getRange(spreadsheetId, courseName, "A:ZZ")).willReturn(sheetData);

                    this.coursesRepository.updateCourseData(courseName, dates.get(1), updateData);

                    verify(this.googleSheetsApi).updateDataRange(spreadsheetId, courseName, range, dataToWriteToSpreadsheet);
                });
            });

        });
    }
}
