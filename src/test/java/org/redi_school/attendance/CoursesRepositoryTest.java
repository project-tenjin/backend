package org.redi_school.attendance;

import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.SheetProperties;
import com.mscharhag.oleaster.runner.OleasterRunner;
import org.junit.runner.RunWith;
import org.springframework.mock.env.MockEnvironment;

import java.util.Arrays;
import java.util.List;

import static com.mscharhag.oleaster.runner.StaticRunnerSupport.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

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

                it("filters out non-course sheets", () -> {
                    given(this.googleSheetsApi.getSheets(spreadsheetId))
                            .willReturn(Arrays.asList(buildSheet(0, "Attendance key"), buildSheet(1, "CourseSummary A"), buildSheet(2, "CourseSummary B")));

                    assertThat(this.coursesRepository.getCourses())
                            .isEqualTo(Arrays.asList(new CourseSummary(1, "CourseSummary A"), new CourseSummary(2, "CourseSummary B")));
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
            it("returns details about a course", () -> {
                String courseName = "CourseSummary 1";
                List<String> students = Arrays.asList("Student 1", "Student 2");

                List<List<Object>> sheetData = Arrays.asList(
                        Arrays.asList(""),
                        Arrays.asList(""), // Dates
                        Arrays.asList(""), // Day of week
                        Arrays.asList("", "Student 1"),
                        Arrays.asList("", "Student 2")
                );

                given(this.googleSheetsApi.getRange(spreadsheetId, courseName, "A:ZZ"))
                        .willReturn(sheetData);

                assertThat(this.coursesRepository.getCourseDetails(courseName))
                        .isEqualTo(new CourseDetails(courseName, students));
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
                        .isEqualTo(new CourseDetails(courseName, students));
            });
        });
    });
}}
