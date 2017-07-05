package org.redi_school.attendance;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ThanksController {
    @GetMapping("/thanks")
    String showThanksPage() {
        return "thanksPage";
    }
}
