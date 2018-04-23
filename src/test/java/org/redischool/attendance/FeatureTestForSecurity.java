package org.redischool.attendance;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(
        webEnvironment = DEFINED_PORT,
        properties = {
                "credentials.username=foo",
                "credentials.password=bar"
        }
)
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("cloud")
public class FeatureTestForSecurity extends FeatureTestScaffolding {

    private static WebDriver driver;

    @Value("${okta.test.username}")
    private String username;

    @Value("${okta.test.password}")
    private String password;

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
    public void testHasAccessOnlyToCoursesAssigned() throws Exception {
        // redi-project-tenjin@googlegroups.com has access to Chasing Unicorns but not to Android App. Configured in okta admin console with group settings
        loginViaOkta(driver);
        selectCourse(COURSE_NAME);
        assertThat($("h1#courseName").first().text()).isEqualTo(COURSE_NAME);

        goTo(getBaseUrl());
        selectCourse("Android App");
        assertThat($("h1").first().text()).isEqualTo("You do not have permissions to view this course.");

    }

    private void loginViaOkta(WebDriver driver) {
        goTo(getBaseUrl());
        $("#okta-signin-username").fill().withText(username);
        $("#okta-signin-password").fill().withText(password);
        $("#okta-signin-submit").submit();
        WebDriverWait wait = new WebDriverWait(driver, 5);
        wait.until((fluentControl) -> driver.getCurrentUrl().contains("localhost"));
    }
}
