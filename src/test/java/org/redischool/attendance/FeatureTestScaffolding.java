package org.redischool.attendance;

import io.github.bonigarcia.wdm.PhantomJsDriverManager;
import org.fluentlenium.adapter.FluentAdapter;
import org.junit.BeforeClass;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.web.server.LocalServerPort;

public abstract class FeatureTestScaffolding extends FluentAdapter {

	protected static final String COURSE_NAME = "Chasing Unicorns";

	@LocalServerPort
	protected int port;

	@BeforeClass
	public static void setupClass() {
		PhantomJsDriverManager.getInstance().setup();
	}

	protected WebDriver createWebDriver() {
		DesiredCapabilities capabilities = new DesiredCapabilities();
		PhantomJSDriverService service = PhantomJSDriverService.createDefaultService(capabilities);

		return new PhantomJSDriver(service, capabilities);
	}

	protected void selectCourse(String courseName) {
		find(By.xpath("//a[text()='" + courseName + "']")).click();
	}
}
