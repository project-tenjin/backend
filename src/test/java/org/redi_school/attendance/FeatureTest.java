package org.redi_school.attendance;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@RunWith(SpringJUnit4ClassRunner.class)
public class FeatureTest {

    @LocalServerPort
    int port;

    @Test
    public void setup() throws Exception {
        WebClient webClient = new WebClient();
        Page page = webClient.getPage("http://localhost:" + port + "/");
        assertThat(page.getWebResponse().getContentAsString()).contains("Chasing Unicorns");
    }
}
