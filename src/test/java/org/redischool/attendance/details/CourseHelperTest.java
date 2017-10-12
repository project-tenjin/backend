package org.redischool.attendance.details;

import com.mscharhag.oleaster.runner.OleasterRunner;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.mscharhag.oleaster.runner.StaticRunnerSupport.*;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Java6Assertions.assertThat;

@RunWith(OleasterRunner.class)
public class CourseHelperTest {

    private CourseHelper courseHelper;
    private CourseDetails courseDetails;

    {
        describe("CourseHelper", () -> {
            beforeEach(() -> {
                courseHelper = new CourseHelper();
            });

            describe("closestCourseDate", () -> {
                describe("when course has dates", () -> {
                    beforeEach(() -> {
                        List<Date> dates = Arrays.asList(
                                new Date(1512259200000L), // 2017, 12, 3
                                new Date(1512432000000L), // 2017, 12, 5
                                new Date(1512604800000L)  // 2017, 12, 7
                        );
                        courseDetails = new CourseDetails("", emptyList(), emptyList(), dates);
                    });

                    it("returns same date if given date is also a course date", () -> {
                        String result = courseHelper.closestCourseDate(new Date(1512432000000L), courseDetails);
                        assertThat(result).isEqualTo("2017-12-05T00:00Z");
                    });

                    it("returns most recent occurrence date if given date is in between course dates", () -> {
                        String result = courseHelper.closestCourseDate(new Date(1512518400000L), courseDetails);
                        assertThat(result).isEqualTo("2017-12-05T00:00Z");
                    });

                    it("returns last course date if given date is after course ended", () -> {
                        String result = courseHelper.closestCourseDate(new Date(1512777600000L), courseDetails);
                        assertThat(result).isEqualTo("2017-12-07T00:00Z");
                    });

                    it("returns first course date if given date is before course starts", () -> {
                        String result = courseHelper.closestCourseDate(new Date(1512172800000L), courseDetails);
                        assertThat(result).isEqualTo("2017-12-03T00:00Z");
                    });
                });

                describe("when course has NO dates", () -> {
                    beforeEach(() -> {
                        courseDetails = new CourseDetails("", emptyList(), emptyList(), emptyList());
                    });

                    it("returns null", () -> {
                        String result = courseHelper.closestCourseDate(new Date(2017, 12, 3), courseDetails);
                        assertThat(result).isNull();
                    });
                });
            });
        });
    }
}
