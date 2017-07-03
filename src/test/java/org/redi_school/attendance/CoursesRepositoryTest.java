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
            describe("when there are tabs in the targeted spreadsheet", () -> {
                it("returns the tabs names", () -> {
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
            });

            describe("when there are no tabs in the targeted spreadsheet", () -> {
                beforeEach(() -> {
                    given(this.googleSheetsApi.getSheets(spreadsheetId))
                            .willReturn(Arrays.asList());
                });

                it("returns the tabs names", () -> {
                    assertThat(this.coursesRepository.getCourses())
                            .isEmpty();
                });
            });
        });
    });
}}
