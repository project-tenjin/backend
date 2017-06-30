package org.redi_school.attendance;

import com.mscharhag.oleaster.runner.OleasterRunner;
import org.junit.runner.RunWith;

import java.util.Arrays;

import static com.mscharhag.oleaster.runner.StaticRunnerSupport.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@RunWith(OleasterRunner.class)
public class CoursesRepositoryTest {
    CoursesRepository coursesRepository;
    GoogleSheetsApi googleSheetsApi;
{
    describe("CoursesRepository", () -> {
        beforeEach(() -> {
            this.googleSheetsApi = mock(GoogleSheetsApi.class);
            this.coursesRepository = new CoursesRepository(googleSheetsApi);
        });

        describe("fetching information from the courses Google spreadsheet", () -> {
            describe("when there are tabs in the targeted spreadsheet", () -> {
                beforeEach(() -> {
                    given(this.googleSheetsApi.getSheetNames())
                            .willReturn(Arrays.asList("Course A", "Course B"));
                });

                it("returns the tabs names", () -> {
                    assertThat(this.coursesRepository.getCourses())
                            .isEqualTo(Arrays.asList("Course A", "Course B"));
                });
            });

            describe("when there are no tabs in the targeted spreadsheet", () -> {
                beforeEach(() -> {
                    given(this.googleSheetsApi.getSheetNames())
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
