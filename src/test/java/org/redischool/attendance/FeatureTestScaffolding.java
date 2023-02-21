package org.redischool.attendance;

import io.github.bonigarcia.wdm.config.DriverManagerType;
import io.github.bonigarcia.wdm.managers.PhantomJsDriverManager;
import org.fluentlenium.adapter.FluentAdapter;
import org.junit.jupiter.api.BeforeAll;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.springframework.boot.web.server.LocalServerPort;

public abstract class FeatureTestScaffolding extends FluentAdapter {

	protected static final String COURSE_NAME = "Chasing Unicorns";

	@LocalServerPort
	protected int port;

	@BeforeAll
	public static void setupClass() {
		PhantomJsDriverManager.getInstance(DriverManagerType.PHANTOMJS).setup();
	}

	protected WebDriver createWebDriver() {
		DesiredCapabilities capabilities = new DesiredCapabilities();
		PhantomJSDriverService service = PhantomJSDriverService.createDefaultService(capabilities);

		return new PhantomJSDriver(service, capabilities);
	}

	protected void selectCourse(String courseName) {
		find(By.xpath("//a[text()='" + courseName + "']")).click();
	}

	protected boolean isCourseVisible(String courseName) {
		return !find(By.xpath("//a[text()='" + courseName + "']")).isEmpty();
	}
}
