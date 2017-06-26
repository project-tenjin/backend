package attendance;

import org.springframework.boot.autoconfigure.*;
import org.springframework.stereotype.*;
import org.springframework.web.bind.annotation.*;

@Controller
@EnableAutoConfiguration
public class WelcomeController {
    @RequestMapping("/")
    @ResponseBody
    String home() {
        return "Hello World!";
    }
}
