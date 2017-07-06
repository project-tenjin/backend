package org.redi_school.attendance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;


@Controller
public class CoursesController {

    private CoursesRepository coursesRepository;

    @Autowired
    public CoursesController(CoursesRepository coursesRepository) {
        this.coursesRepository = coursesRepository;
    }

    @GetMapping("/")
    String listCourses(Model model) {
        model.addAttribute("courses", coursesRepository.getCourses());
        return "courseList";
    }

    @GetMapping("/courses/{id}")
    String showCourse(@PathVariable int id, Model model) {
        CourseSummary currentCourseSummary = coursesRepository.getCourses().stream()
                .filter((CourseSummary course) -> course.getId() == id)
                .findFirst().get();
        CourseDetails courseDetails = coursesRepository.getCourseDetails(currentCourseSummary.getName());
        model.addAttribute("courseDetails", courseDetails);
        return "courseDetail";
    }

    @PostMapping("/courses/{id}")
    String postCourseAttendance(@ModelAttribute CourseAttendanceForm courseAttendanceForm) {
        this.coursesRepository.updateCourseData(courseAttendanceForm.getCourseName(),
                courseAttendanceForm.getDate(),
                courseAttendanceForm.getAttendances());
        return "redirect:/thanks";
    }

    @ExceptionHandler(NoSuchElementException.class)
    ResponseEntity<String> handleNoSuchElementException() {
        return new ResponseEntity<String>(HttpStatus.NOT_FOUND);
    }
}


