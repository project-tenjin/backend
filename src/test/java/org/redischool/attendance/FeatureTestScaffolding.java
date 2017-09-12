package org.redischool.attendance;

import io.github.bonigarcia.wdm.PhantomJsDriverManager;
import org.fluentlenium.adapter.FluentAdapter;
import org.junit.BeforeClass;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.service.DriverService;
import org.springframework.boot.context.embedded.LocalServerPort;

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
		DriverService service = PhantomJSDriverService.createDefaultService(capabilities);

		return new PhantomJSDriver(service, capabilities);
	}

}
