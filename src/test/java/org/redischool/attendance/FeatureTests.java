package org.redischool.attendance;

import io.github.bonigarcia.wdm.PhantomJsDriverManager;
import org.fluentlenium.adapter.FluentAdapter;
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
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@RunWith(SpringJUnit4ClassRunner.class)
public class FeatureTests extends FluentAdapter {

    public static final int NUMBER_OF_RADIOBUTTON_CHOICES = 3;

    private static final String COURSE_NAME = "Chasing Unicorns";
    private static final String OTHER_COURSE_NAME = "App Design";
    private static final int NUMBER_OF_STUDENTS_IN_COURSE = 9;

    @LocalServerPort
    private int port;

    @Autowired
    private Environment environment;

    @Autowired
    private GoogleSheetsApi googleSheetsApi;

    private static WebDriver driver;

    @BeforeClass
    public static void setupClass() {
        PhantomJsDriverManager.getInstance().setup();
    }

    @Before
    public void setUp() throws Exception {
        if (null == driver) {
            DesiredCapabilities capabilities = new DesiredCapabilities();
            DriverService service = PhantomJSDriverService.createDefaultService(capabilities);

            driver = new PhantomJSDriver(service, capabilities);
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

        assertThat($("tr:nth-child(2) > th:nth-child(1)").text()).isEqualTo("Student 1");
        assertThat($("tr:last-child > th:nth-child(1)").text()).isEqualTo("Student 9");
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
    public void testWeSeeASuccessMessageWhenWeSubmitTheDataFromTheCoursePage() throws Exception {
        goTo(getBaseUrl());
        selectCourse(COURSE_NAME);

        selectDate();
        List<String> selectedAttendanceStates = randomlySelectStudentAttendanceStates();
        $("#submit").click();

        assertThat(getDriver().getCurrentUrl()).endsWith("/thanks");
        assertThat($("body").text()).contains("Thanks");

        List<List<Object>> attendanceStateInSpreadsheet = googleSheetsApi.getRange(environment.getProperty("google.spreadsheet.id"), COURSE_NAME, "D4:D12");
        for (int i = 0; i < selectedAttendanceStates.size(); i++) {
            assertThat(attendanceStateInSpreadsheet.get(i).get(0))
                    .isEqualTo(selectedAttendanceStates.get(i));
        }
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

    private void selectDate() {
        String date = "4/27";
        $("select[name=date]").fillSelect().withText(date);
    }

    private String intToAttendanceState(int randomRadioIndex) {
        String attendanceState = "";
        switch (randomRadioIndex) {
            case 1:
                attendanceState = "P";
                break;
            case 2:
                attendanceState = "L";
                break;
            case 3:
                attendanceState = "U";
                break;
        }
        return attendanceState;
    }

    private void selectCourse(String courseName) {
        find(By.xpath("//a[text()='" + courseName + "']")).click();
    }
}
