package org.redischool.attendance.summary;

import org.redischool.attendance.details.CourseAccessValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class CourseSummaryController {

    private final CourseSummaryRepository courseSummaryRepository;
    private final CourseAccessValidator courseAccessValidator;

    @Autowired
    public CourseSummaryController(CourseSummaryRepository courseSummaryRepository, CourseAccessValidator courseAccessValidator) {
        this.courseSummaryRepository = courseSummaryRepository;
        this.courseAccessValidator = courseAccessValidator;
    }

    @GetMapping("/")
    public String listCourses(Model model, Authentication authentication) {
        final List<CourseSummary> courses = courseSummaryRepository
                .getCourses()
                .stream()
                .filter(course -> courseAccessValidator.hasPermissions(authentication, course.getName()))
                .collect(Collectors.toList());

        model.addAttribute("courses", courses);
        return "courseList";
    }
}
