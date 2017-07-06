package org.redi_school.attendance;

import com.mscharhag.oleaster.runner.OleasterRunner;
import org.junit.runner.RunWith;

import static com.mscharhag.oleaster.runner.StaticRunnerSupport.*;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(OleasterRunner.class)
public class SpreadsheetColumnNameMapperTest {
    SpreadsheetColumnNameMapper mapper;
    {
        describe("SpreadsheetColumnNameMapper", () -> {
            beforeEach(() -> {
                mapper = new SpreadsheetColumnNameMapper();
            });

            describe("index to column char", () -> {
                it("translates correctly", () -> {
                    assertThat(this.mapper.columnIndexToLetter(1)).isEqualTo("A");
                    assertThat(this.mapper.columnIndexToLetter(19)).isEqualTo("S");
                    assertThat(this.mapper.columnIndexToLetter(26)).isEqualTo("Z");
                    assertThat(this.mapper.columnIndexToLetter(27)).isEqualTo("AA");
                    assertThat(this.mapper.columnIndexToLetter(28)).isEqualTo("AB");
                    assertThat(this.mapper.columnIndexToLetter(29)).isEqualTo("AC");
                    assertThat(this.mapper.columnIndexToLetter(52)).isEqualTo("AZ");
                });
            });
        });
    }
}
