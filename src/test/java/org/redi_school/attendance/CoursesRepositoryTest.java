package org.redi_school.attendance;

import com.mscharhag.oleaster.runner.OleasterRunner;
import org.junit.runner.RunWith;
import org.springframework.mock.env.MockEnvironment;

import java.util.Arrays;

import static com.mscharhag.oleaster.runner.StaticRunnerSupport.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@RunWith(OleasterRunner.class)
public class CoursesRepositoryTest {
    CoursesRepository coursesRepository;
    GoogleSheetsApi googleSheetsApi;
    MockEnvironment mockEnvironment;
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
                            .willReturn(Arrays.asList(new Sheet(0, "Course A"), new Sheet(0, "Course B")));

                    assertThat(this.coursesRepository.getCourses())
                            .isEqualTo(Arrays.asList(new Sheet(0, "Course A"), new Sheet(0, "Course B")));
                });

                it("filters out non-course sheets", () -> {
                    given(this.googleSheetsApi.getSheets(spreadsheetId))
                            .willReturn(Arrays.asList(new Sheet(0, "Attendance key"), new Sheet(0, "Course A"), new Sheet(0, "Course B")));

                    assertThat(this.coursesRepository.getCourses())
                            .isEqualTo(Arrays.asList(new Sheet(0, "Course A"), new Sheet(0, "Course B")));
                });

                describe("when there are students for a tab", () -> {
                    it("returns them", () -> {
                        String courseName = "Course 1";
                        given(this.googleSheetsApi.getRange(spreadsheetId, courseName, "B:B"))
                                .willReturn(Arrays.asList(
                                        Arrays.asList(""),
                                        Arrays.asList(""),
                                        Arrays.asList("STUDENT"),
                                        Arrays.asList("Student 1"),
                                        Arrays.asList("Student 2")));

                        assertThat(this.coursesRepository.getStudentsForCourse(courseName))
                                .isEqualTo(Arrays.asList("Student 1", "Student 2"));
                    });
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
    });
}}
