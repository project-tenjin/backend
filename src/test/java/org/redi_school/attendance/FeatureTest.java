package org.redi_school.attendance;

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
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@RunWith(SpringJUnit4ClassRunner.class)
public class FeatureTest extends FluentAdapter {

    private static WebDriver driver;

    @LocalServerPort
    private int port;
    private String COURSE_NAME = "Chasing Unicorns";
    private String OTHER_COURSE_NAME = "App Design";

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
    }

    @AfterClass
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    public void testListOfCoursesIsDisplayed() throws Exception {
        goTo("http://localhost:" + port + "/");

        assertThat($("ul").text()).contains(COURSE_NAME);
        assertThat($("ul").text()).contains(OTHER_COURSE_NAME);
    }

    @Test
    public void testListOfStudentsIsDisplayedOnCoursePage() throws Exception {
        goTo("http://localhost:" + port + "/");
        find(By.xpath("//a[text()='Chasing Unicorns']")).click();

        assertThat($("h1#courseName").text()).isEqualTo(COURSE_NAME);
        assertThat($("li:first-of-type>div:first-of-type").text()).isEqualTo("Student 1");
        assertThat($("li:last-of-type>div:first-of-type").text()).isEqualTo("Student 9");
    }

    @Test
    public void testFormForAttendanceStatusIsDisplayedPerStudentOnCoursePage() throws Exception {
        goTo("http://localhost:" + port + "/");
        find(By.xpath("//a[text()='Chasing Unicorns']")).click();

        assertThat($("form")).isNotEmpty();
        assertThat($("li:first-of-type>div>input[type=radio]:nth-of-type(1)").value()).contains("P");
        assertThat($("li:first-of-type>div>input[type=radio]:nth-of-type(2)").value()).contains("L");
        assertThat($("li:first-of-type>div>input[type=radio]:nth-of-type(3)").value()).contains("U");
        assertThat($("li:first-of-type>div>input[type=radio]:nth-of-type(4)").value()).contains("E");
    }
}
