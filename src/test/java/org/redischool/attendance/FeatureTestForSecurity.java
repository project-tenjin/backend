package org.redischool.attendance;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(
        webEnvironment = RANDOM_PORT,
        properties = {
                "credentials.username=foo",
                "credentials.password=bar"
        }
)
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("cloud")
public class FeatureTestForSecurity extends FeatureTestScaffolding {

    protected static WebDriver driver;

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
    public void testCourseDetailsAreProtectedByBasicAuth() throws Exception {
        goTo("http://localhost:" + port + "/");
        find(By.xpath("//a[text()='" + COURSE_NAME + "']")).click();
        assertThat($("h1#courseName").text()).isNull();

        goTo("http://foo:bar@localhost:" + port + "/");
        find(By.xpath("//a[text()='" + COURSE_NAME + "']")).click();
        assertThat($("h1#courseName").text()).isEqualTo(COURSE_NAME);
    }
}
