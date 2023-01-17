package org.redischool.attendance;

import com.google.common.collect.ImmutableMap;
import org.fluentlenium.core.domain.FluentList;
import org.fluentlenium.core.domain.FluentWebElement;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.redischool.attendance.spreadsheet.GoogleSheetsApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("test")
public class FeatureTests extends FeatureTestScaffolding {

    private static WebDriver driver;

    private static final int NUMBER_OF_RADIOBUTTON_CHOICES = 3;

    private static final String OTHER_COURSE_NAME = "App Design";

    private static final int NUMBER_OF_STUDENTS_IN_COURSE = 9;

    private static final String DATE_SELECTED = "4/27";

    private static final String FIRST_STUDENT = "Student 1";
    private static final String LAST_STUDENT = "Student 9";

    private static final Map<Integer, String> attendanceValueByRadioIndex =
            ImmutableMap.<Integer, String>builder()
                    .put(1, "P")
                    .put(2, "L")
                    .put(3, "U")
                    .build();

    @Autowired
    private GoogleSheetsApi googleSheetsApi;

    @Value("${google.spreadsheet.id}")
    private String spreadSheetId;

    @Before
    public void setUp() {
        if (driver == null) {
            driver = createWebDriver();
        }

        initFluent(driver);
        setBaseUrl("http://localhost:" + port + "/");
    }

    @AfterClass
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    public void testListOfCoursesIsDisplayed() {
        goTo(getBaseUrl());

        assertThat($("ul").first().text()).contains(COURSE_NAME);
        assertThat($("ul").last().text()).contains(OTHER_COURSE_NAME);
    }

    @Test
    public void testListOfStudentsIsDisplayedOnCoursePage() {
        goTo(getBaseUrl());
        selectCourse(COURSE_NAME);

        assertThat($("h1#courseName").first().text()).isEqualTo(COURSE_NAME);

        assertThat($("tr:nth-child(2) > td:nth-child(1)").first().text()).isEqualTo(FIRST_STUDENT);
        assertThat($("tr:last-child > td:nth-child(1)").first().text()).isEqualTo(LAST_STUDENT);
    }

    @Test
    public void testFormForAttendanceStatusIsDisplayedPerStudentOnCoursePage() {
        goTo(getBaseUrl());
        selectCourse(COURSE_NAME);

        assertThat($("form")).isNotEmpty();
        assertThat($("table > tbody > tr:nth-of-type(2) > td:nth-of-type(2) > input").first().value()).isEqualTo("P");
        assertThat($("table > tbody > tr:nth-of-type(2) > td:nth-of-type(3) > input").first().value()).isEqualTo("L");
        assertThat($("table > tbody > tr:nth-of-type(2) > td:nth-of-type(4) > input").first().value()).isEqualTo("U");
    }

    @Test
    public void testDatePickerIsDisplayedOnCoursePageWithDatesFromSpreadsheet() {
        goTo(getBaseUrl());
        selectCourse(COURSE_NAME);

        assertThat($("select")).isNotEmpty();

        assertThat($("select > option:first-of-type").first().text()).isEqualTo("Please select a date");
        assertThat($("select > option:nth-of-type(2)").first().text()).isEqualTo("4/24");
        assertThat($("select > option:last-of-type").first().text()).isEqualTo("7/13");
    }

    @Test
    public void testAttendanceIsRefreshedWhenDateIsSelected() {
        goTo(getBaseUrl());
        selectCourse(COURSE_NAME);

        assertTrue(radioButtonNotSelected());

        selectDate();

        waitUntilRadioButtonIsSelected();
    }

    @Test
    public void testWeSeeASuccessMessageWhenWeSubmitTheDataFromTheCoursePage() {
        goTo(getBaseUrl());
        selectCourse(COURSE_NAME);

        selectDate();

        waitUntilRadioButtonIsSelected();

        List<String> selectedAttendanceStates = randomlySelectStudentAttendanceStates();
        $("#submit").click();

        assertThat(getDriver().getCurrentUrl()).endsWith("/thanks");
        assertThat($("body").first().text()).contains("Thanks");

        List<List<Object>> attendanceStateInSpreadsheet = fetchAttendanceFromSpreadsheet();
        for (int i = 0; i < selectedAttendanceStates.size(); i++) {
            assertThat(attendanceStateInSpreadsheet.get(i).get(0))
                    .isEqualTo(selectedAttendanceStates.get(i));
        }
    }

    private List<List<Object>> fetchAttendanceFromSpreadsheet() {
        final List<List<Object>> attendance = googleSheetsApi.getRange(spreadSheetId, COURSE_NAME, "B4:D13");

        //filter out dropped out
        return attendance.stream()
                .filter(row -> !((String) row.get(0)).contains("*"))
                .map(row -> Collections.singletonList(row.get(2)))
                .collect(toList());
    }

    private void waitUntilRadioButtonIsSelected() {
        await().atMost(10, SECONDS)
                .untilPredicate((fluentControl) -> radioButtonSelected());
    }

    private boolean radioButtonNotSelected() {
        FluentList<FluentWebElement> radioStudent1 = find(By.cssSelector("input[name='attendances["+FIRST_STUDENT+"]']"));
        return !radioStudent1.get(0).selected() &&
                !radioStudent1.get(1).selected() &&
                !radioStudent1.get(2).selected();
    }

    private boolean radioButtonSelected() {
        FluentList<FluentWebElement> radioStudent1 = find(By.cssSelector("input[name='attendances["+FIRST_STUDENT+"]']"));
        return radioStudent1.get(0).selected() ||
			   radioStudent1.get(1).selected() ||
			   radioStudent1.get(2).selected();
    }

    private List<String> randomlySelectStudentAttendanceStates() {
        List<String> selectedAttendanceStates = new ArrayList<>();

        for (int student = 1; student <= NUMBER_OF_STUDENTS_IN_COURSE; student++) {
            int randomRadioIndex = ThreadLocalRandom.current().nextInt(1, NUMBER_OF_RADIOBUTTON_CHOICES + 1);
            int trStudentIndex = student + 1; // Index 1 is the header tr with P L U.
            int thRadioButtonIndex = randomRadioIndex + 1; // Index 1 is the name th.
            $("table > tbody > tr:nth-of-type(" + trStudentIndex + ") > td:nth-of-type(" + thRadioButtonIndex +") > input").click();
            selectedAttendanceStates.add(intToAttendanceState(randomRadioIndex));
        }
        return selectedAttendanceStates;
    }

    private void selectDate() {
        $("select[name=date]").fillSelect().withText(DATE_SELECTED);
    }

    private String intToAttendanceState(int randomRadioIndex) {
        return attendanceValueByRadioIndex.getOrDefault(randomRadioIndex, "");
    }
}
