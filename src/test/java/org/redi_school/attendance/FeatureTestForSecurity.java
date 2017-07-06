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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("cloud")
public class FeatureTestForSecurity extends FluentAdapter {

    private static WebDriver driver;

    @LocalServerPort
    private int port;
    private String COURSE_NAME = "Chasing Unicorns";

    static {
        System.setProperty("credentials.username", "foo");
        System.setProperty("credentials.password", "bar");
    }

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
    public void testCourseDetailsAreProtectedByBasicAuth() throws Exception {
        goTo("http://localhost:" + port + "/");
        find(By.xpath("//a[text()='" + COURSE_NAME + "']")).click();
        assertThat($("h1#courseName").text()).isNull();

        goTo("http://foo:bar@localhost:" + port + "/");
        find(By.xpath("//a[text()='" + COURSE_NAME + "']")).click();
        assertThat($("h1#courseName").text()).isEqualTo(COURSE_NAME);
    }
}
