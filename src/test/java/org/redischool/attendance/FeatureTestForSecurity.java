package org.redischool.attendance;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;

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

    // redi-project-tenjin@googlegroups.com has access to Chasing Unicorns but not to Android App. Configured in okta admin console with group settings
    private static final String COURSE_NAME_I_CANNOT_ACCESS = "Android App";

    private static WebDriver driver;

    @Value("${okta.test.username}")
    private String username;

    @Value("${okta.test.password}")
    private String password;

    @Before
    public void setUp() {
        boolean firstRun = driver == null;

        if (firstRun) {
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
    public void testSeesOnlyAssignedCourses() {
        // ACT
        goHome();

        // ASSERT
        assertThat(isCourseVisible(COURSE_NAME)).isTrue();
        assertThat(isCourseVisible(COURSE_NAME_I_CANNOT_ACCESS)).isFalse();
    }

    @Test
    public void testHasAccessOnlyToCoursesAssigned() {
        // ARRANGE
        goHome();

        // ACT
        selectCourse(COURSE_NAME);

        // ASSERT
        assertThat($("h1#courseName").first().text()).isEqualTo(COURSE_NAME);
    }

    @Test
    public void testHasNoAccessToCoursersNotAssigned() {
        // ARRANGE
        goHome();

        // ACT
        goTo(getBaseUrl() + "courses?name=" + URLEncoder.encode(COURSE_NAME_I_CANNOT_ACCESS, StandardCharsets.UTF_8));

        // ASSERT
        assertThat($("h1").first().text()).isEqualTo("You do not have permissions to view this course.");
    }

    private void goHome() {
        goTo(getBaseUrl());

        if (!authenticated()) {
            loginViaOkta();
        }
    }

    private boolean authenticated() {
        return !url().contains("oktapreview");
    }

    private void loginViaOkta() {
        $("#okta-signin-username").fill().withText(username);
        $("#okta-signin-password").fill().withText(password);
        $("#okta-signin-submit").click();
        WebDriverWait wait = new WebDriverWait(driver, 10);
        wait.until((fluentControl) -> driver.getCurrentUrl().contains("localhost"));
    }
}
