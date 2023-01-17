package org.redischool.attendance.summary;

import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.SheetProperties;
import com.mscharhag.oleaster.runner.OleasterRunner;
import org.junit.runner.RunWith;
import org.redischool.attendance.spreadsheet.GoogleSheetsApi;

import static com.mscharhag.oleaster.runner.StaticRunnerSupport.*;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@RunWith(OleasterRunner.class)
public class CourseSummaryRepositoryTest {

    private CourseSummaryRepository courseSummaryRepository;
    private GoogleSheetsApi googleSheetsApi;

    private Sheet buildSheet(int id, String name) {
        Sheet sheet = new Sheet();
        SheetProperties properties = new SheetProperties();
        properties.setSheetId(id);
        properties.setTitle(name);
        sheet.setProperties(properties);

        return sheet;
    }

    {
        describe("CourseSummaryRepository", () -> {
            final String spreadsheetId = "meehp";

            beforeEach(() -> {
                googleSheetsApi = mock(GoogleSheetsApi.class);

                courseSummaryRepository = new CourseSummaryRepository(googleSheetsApi, spreadsheetId);

            });

            describe("fetching information from the courses Google spreadsheet", () -> {
                describe("when there are sheets in the targeted spreadsheet", () -> {
                    it("returns the sheet names", () -> {
                        given(googleSheetsApi.getSheets(spreadsheetId))
                                .willReturn(asList(buildSheet(0, "CourseSummary A"), buildSheet(1, "CourseSummary B")));

                        assertThat(courseSummaryRepository.getCourses())
                                .isEqualTo(asList(new CourseSummary(0, "CourseSummary A"), new CourseSummary(1, "CourseSummary B")));
                    });

                    it("filters out non-course sheets with asterisk in the name", () -> {
                        given(googleSheetsApi.getSheets(spreadsheetId))
                                .willReturn(asList(
                                        buildSheet(0, "*Attendance key"),
                                        buildSheet(1, "CourseSummary A"),
                                        buildSheet(2, "CourseSummary B"),
                                        buildSheet(3, "Hide me*"),
                                        buildSheet(4, "CourseSummary C")));

                        assertThat(courseSummaryRepository.getCourses())
                                .isEqualTo(asList(
                                        new CourseSummary(1, "CourseSummary A"),
                                        new CourseSummary(2, "CourseSummary B"),
                                        new CourseSummary(4, "CourseSummary C")));
                    });
                });

                describe("when there are no sheets in the targeted spreadsheet", () -> {
                    beforeEach(() -> {
                        given(googleSheetsApi.getSheets(spreadsheetId))
                                .willReturn(asList());
                    });

                    it("returns the sheet names", () -> {
                        assertThat(courseSummaryRepository.getCourses())
                                .isEmpty();
                    });
                });
            });

        });
    }
}
