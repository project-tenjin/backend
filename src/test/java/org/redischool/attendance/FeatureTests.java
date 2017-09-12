package org.redischool.attendance;

import com.google.common.collect.ImmutableMap;
import io.github.bonigarcia.wdm.PhantomJsDriverManager;
import org.fluentlenium.adapter.FluentAdapter;
import org.fluentlenium.core.domain.FluentList;
import org.fluentlenium.core.domain.FluentWebElement;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.service.DriverService;
import org.redischool.attendance.spreadsheet.GoogleSheetsApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static java.util.concurrent.TimeUnit.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@RunWith(SpringJUnit4ClassRunner.class)
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
    public void setUp() throws Exception {
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
    public void testListOfCoursesIsDisplayed() throws Exception {
        goTo(getBaseUrl());

        assertThat($("ul").text()).contains(COURSE_NAME);
        assertThat($("ul").text()).contains(OTHER_COURSE_NAME);
    }

    @Test
    public void testListOfStudentsIsDisplayedOnCoursePage() throws Exception {
        goTo(getBaseUrl());
        selectCourse(COURSE_NAME);

        assertThat($("h1#courseName").text()).isEqualTo(COURSE_NAME);

        assertThat($("tr:nth-child(2) > th:nth-child(1)").text()).isEqualTo(FIRST_STUDENT);
        assertThat($("tr:last-child > th:nth-child(1)").text()).isEqualTo(LAST_STUDENT);
    }

    @Test
    public void testFormForAttendanceStatusIsDisplayedPerStudentOnCoursePage() throws Exception {
        goTo(getBaseUrl());
        selectCourse(COURSE_NAME);

        assertThat($("form")).isNotEmpty();
        assertThat($("table > tbody > tr:nth-of-type(2) > th:nth-of-type(2) > input").value()).isEqualTo("P");
        assertThat($("table > tbody > tr:nth-of-type(2) > th:nth-of-type(3) > input").value()).isEqualTo("L");
        assertThat($("table > tbody > tr:nth-of-type(2) > th:nth-of-type(4) > input").value()).isEqualTo("U");
    }

    @Test
    public void testDatePickerIsDisplayedOnCoursePageWithDatesFromSpreadsheet() throws Exception {
        goTo(getBaseUrl());
        selectCourse(COURSE_NAME);

        assertThat($("select")).isNotEmpty();

        assertThat($("select > option:first-of-type").text()).isEqualTo("Please select a date");
        assertThat($("select > option:nth-of-type(2)").text()).isEqualTo("4/24");
        assertThat($("select > option:last-of-type").text()).isEqualTo("7/13");
    }

    @Test
    public void testAttendanceIsRefreshedWhenDateIsSelected() throws Exception {
        goTo(getBaseUrl());
        selectCourse(COURSE_NAME);

        assertTrue(radioButtonNotSelected());

        selectDate();

        waitUntilRadioButtonIsSelected();
    }

    @Test
    public void testWeSeeASuccessMessageWhenWeSubmitTheDataFromTheCoursePage() throws Exception {
        goTo(getBaseUrl());
        selectCourse(COURSE_NAME);

        selectDate();

        waitUntilRadioButtonIsSelected();

        List<String> selectedAttendanceStates = randomlySelectStudentAttendanceStates();
        $("#submit").click();

        assertThat(getDriver().getCurrentUrl()).endsWith("/thanks");
        assertThat($("body").text()).contains("Thanks");

        List<List<Object>> attendanceStateInSpreadsheet = googleSheetsApi.getRange(spreadSheetId, COURSE_NAME, "D4:D12");
        for (int i = 0; i < selectedAttendanceStates.size(); i++) {
            assertThat(attendanceStateInSpreadsheet.get(i).get(0))
                    .isEqualTo(selectedAttendanceStates.get(i));
        }
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
            $("table > tbody > tr:nth-of-type(" + trStudentIndex + ") > th:nth-of-type(" + thRadioButtonIndex +") > input").click();
            selectedAttendanceStates.add(intToAttendanceState(randomRadioIndex));
        }
        return selectedAttendanceStates;
    }

    private void selectCourse(String courseName) {
        find(By.xpath("//a[text()='" + courseName + "']")).click();
    }

    private void selectDate() {
        $("select[name=date]").fillSelect().withText(DATE_SELECTED);
    }

    private String intToAttendanceState(int randomRadioIndex) {
        return attendanceValueByRadioIndex.getOrDefault(randomRadioIndex, "");
    }
}