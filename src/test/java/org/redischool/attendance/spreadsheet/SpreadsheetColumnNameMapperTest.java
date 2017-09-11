package org.redischool.attendance.spreadsheet;

import com.mscharhag.oleaster.runner.OleasterRunner;
import org.junit.runner.RunWith;

import static com.mscharhag.oleaster.runner.StaticRunnerSupport.*;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(OleasterRunner.class)
public class SpreadsheetColumnNameMapperTest {

    private SpreadsheetColumnNameMapper mapper;

    {
        describe("SpreadsheetColumnNameMapper", () -> {
            beforeEach(() -> {
                mapper = new SpreadsheetColumnNameMapper();
            });

            describe("index to column char", () -> {
                it("translates correctly", () -> {
                    assertThat(mapper.columnIndexToLetter(1)).isEqualTo("A");
                    assertThat(mapper.columnIndexToLetter(19)).isEqualTo("S");
                    assertThat(mapper.columnIndexToLetter(26)).isEqualTo("Z");
                    assertThat(mapper.columnIndexToLetter(27)).isEqualTo("AA");
                    assertThat(mapper.columnIndexToLetter(28)).isEqualTo("AB");
                    assertThat(mapper.columnIndexToLetter(29)).isEqualTo("AC");
                    assertThat(mapper.columnIndexToLetter(52)).isEqualTo("AZ");
                });
            });
        });
    }
}
